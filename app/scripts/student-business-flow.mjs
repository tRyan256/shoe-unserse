import crypto from 'node:crypto';
import fs from 'node:fs/promises';
import path from 'node:path';

const BASE_API = process.env.BASE_API || 'http://127.0.0.1:8080';
const USER_SECRET = process.env.JWT_USER_SECRET || 'suwechat';
const ADMIN_SECRET = process.env.JWT_ADMIN_SECRET || 'itcast';
const JWT_TTL_MS = Number(process.env.JWT_TTL_MS || 30 * 24 * 60 * 60 * 1000);

const USER_A_ID = Number(process.env.USER_A_ID || 6);
const USER_B_ID = Number(process.env.USER_B_ID || 7);
const ADMIN_ID = Number(process.env.ADMIN_ID || 1);
const USER_A_PHONE = process.env.USER_A_PHONE || '13800138006';
const USER_B_PHONE = process.env.USER_B_PHONE || '13800138007';

const now = new Date();
const stamp = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}-${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}${String(now.getSeconds()).padStart(2, '0')}`;
const outDir = path.resolve('test-screenshots', `student-flow-${stamp}`);

function getSecretKeyBytes(secret) {
  const src = Buffer.from(secret, 'utf8');
  if (src.length >= 32) return src;
  const secure = Buffer.alloc(32);
  src.copy(secure, 0, 0, src.length);
  for (let i = src.length; i < 32; i += 1) {
    secure[i] = (src[i % src.length] ^ i) & 0xff;
  }
  return secure;
}

function createJwt(claimKey, claimValue, secret, ttlMs) {
  const header = { alg: 'HS256', typ: 'JWT' };
  const payload = { [claimKey]: claimValue, exp: Math.floor((Date.now() + ttlMs) / 1000) };
  const b64u = (obj) => Buffer.from(JSON.stringify(obj)).toString('base64url');
  const signingInput = `${b64u(header)}.${b64u(payload)}`;
  const sig = crypto.createHmac('sha256', getSecretKeyBytes(secret)).update(signingInput).digest('base64url');
  return `${signingInput}.${sig}`;
}

function formatDateTime(value) {
  const date = value instanceof Date ? value : new Date(value);
  const yyyy = date.getFullYear();
  const MM = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  const hh = String(date.getHours()).padStart(2, '0');
  const mm = String(date.getMinutes()).padStart(2, '0');
  const ss = String(date.getSeconds()).padStart(2, '0');
  return `${yyyy}-${MM}-${dd} ${hh}:${mm}:${ss}`;
}

function parseJsonSafe(text) {
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

async function requestApi({ method = 'GET', pathName, token, adminToken, body, params }) {
  const url = new URL(pathName, BASE_API);
  if (params) {
    for (const [key, value] of Object.entries(params)) {
      if (value === undefined || value === null) continue;
      url.searchParams.set(key, String(value));
    }
  }

  const headers = { 'Content-Type': 'application/json' };
  if (token) headers.Authorization = token;
  if (adminToken) headers.token = adminToken;

  const resp = await fetch(url, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const text = await resp.text();
  const payload = parseJsonSafe(text);
  const bizOk = payload?.code === 1;

  return {
    status: resp.status,
    httpOk: resp.ok,
    bizOk,
    code: payload?.code,
    msg: payload?.msg,
    data: payload?.data,
    text,
  };
}

function ensureSuccess(result, label) {
  if (result.httpOk && result.bizOk) return result.data;
  throw new Error(
    `${label}失败: HTTP=${result.status}, code=${result.code ?? 'N/A'}, msg=${result.msg || result.text || '未知错误'}`
  );
}

async function sleep(ms) {
  await new Promise((resolve) => setTimeout(resolve, ms));
}

async function waitFor(task, { timeoutMs = 20000, intervalMs = 1000, name = 'waitFor' } = {}) {
  const start = Date.now();
  let lastError = null;
  while (Date.now() - start < timeoutMs) {
    try {
      return await task();
    } catch (error) {
      lastError = error;
      await sleep(intervalMs);
    }
  }
  throw new Error(`${name}超时: ${lastError instanceof Error ? lastError.message : String(lastError)}`);
}

async function ensureAddress(token, phone, consignee) {
  const listResp = await requestApi({ pathName: '/user/addressBook/list', token });
  const list = ensureSuccess(listResp, '查询地址列表');
  if (Array.isArray(list) && list.length > 0) {
    const defaultAddr = list.find((item) => item.isDefault === 1);
    return defaultAddr?.id || list[0].id;
  }

  await ensureSuccess(
    await requestApi({
      method: 'POST',
      pathName: '/user/addressBook',
      token,
      body: {
        consignee,
        phone,
        sex: '1',
        provinceCode: '北京市',
        provinceName: '北京市',
        cityCode: '北京市',
        cityName: '北京市',
        districtCode: '东城区',
        districtName: '东城区',
        detail: '王府井大街255号',
        label: '家',
        isDefault: 1,
      },
    }),
    '创建地址'
  );

  const retryResp = await requestApi({ pathName: '/user/addressBook/list', token });
  const retryList = ensureSuccess(retryResp, '创建后查询地址列表');
  if (!Array.isArray(retryList) || retryList.length === 0) {
    throw new Error('创建地址后仍未查询到地址');
  }
  const defaultAddr = retryList.find((item) => item.isDefault === 1);
  return defaultAddr?.id || retryList[0].id;
}

async function main() {
  await fs.mkdir(outDir, { recursive: true });

  const userAToken = createJwt('userId', USER_A_ID, USER_SECRET, JWT_TTL_MS);
  const userBToken = createJwt('userId', USER_B_ID, USER_SECRET, JWT_TTL_MS);
  const adminToken = createJwt('empId', ADMIN_ID, ADMIN_SECRET, JWT_TTL_MS);

  const report = {
    startedAt: new Date().toISOString(),
    baseApi: BASE_API,
    users: { userA: USER_A_ID, userB: USER_B_ID, admin: ADMIN_ID },
    results: {
      activityBanner: { ok: false, details: '' },
      drawJoinRevealConfirm: { ok: false, details: '' },
      orderAndMessage: { ok: false, details: '' },
      experience: { ok: false, details: '' },
      airdrop: { ok: false, details: '' },
    },
    artifacts: [],
  };

  try {
    const userAAddressId = await ensureAddress(userAToken, USER_A_PHONE, '孙八');
    const userBAddressId = await ensureAddress(userBToken, USER_B_PHONE, '周九');

    const bannerData = ensureSuccess(
      await requestApi({ pathName: '/user/activity/banner', token: userAToken }),
      '查询活动banner'
    );
    report.results.activityBanner.ok = true;
    report.results.activityBanner.details = `banner数量=${Array.isArray(bannerData) ? bannerData.length : 0}`;

    const spuList = ensureSuccess(
      await requestApi({ pathName: '/user/shoe/list', token: userAToken }),
      '查询鞋款列表'
    );
    if (!Array.isArray(spuList) || spuList.length === 0) {
      throw new Error('未查询到可用鞋款，无法执行联调');
    }

    const spuDetail = ensureSuccess(
      await requestApi({ pathName: `/user/shoe/${spuList[0].id}`, token: userAToken }),
      '查询鞋款详情'
    );
    const firstSku = spuDetail?.skus?.[0];
    const firstSize = firstSku?.sizes?.[0]?.size;
    if (!firstSku?.id || !firstSize) {
      throw new Error('鞋款SKU或尺码缺失，无法执行抽签联调');
    }

    const drawTitle = `学生端联调抽签-${stamp}`;
    const drawStart = new Date(Date.now() - 60 * 1000);
    const drawEnd = new Date(Date.now() + 15 * 60 * 1000);

    ensureSuccess(
      await requestApi({
        method: 'POST',
        pathName: '/admin/draw',
        adminToken,
        body: {
          title: drawTitle,
          targetType: 1,
          skuId: firstSku.id,
          totalStock: 2,
          maxParticipants: 2,
          winnerCount: 1,
          price: firstSku.price,
          startTime: formatDateTime(drawStart),
          endTime: formatDateTime(drawEnd),
          status: 1,
          description: '学生端业务流自动联调抽签活动',
        },
      }),
      '创建联调抽签活动'
    );

    const drawPage = ensureSuccess(
      await requestApi({
        pathName: '/admin/draw/page',
        adminToken,
        params: { page: 1, pageSize: 20, title: drawTitle },
      }),
      '查询联调抽签活动'
    );
    const drawId = drawPage?.records?.find((item) => item.title === drawTitle)?.id;
    if (!drawId) {
      throw new Error('未查询到联调抽签活动ID');
    }

    ensureSuccess(
      await requestApi({
        method: 'POST',
        pathName: '/user/draw/join',
        token: userAToken,
        body: { drawId, shoeSize: firstSize, addressBookId: userAAddressId },
      }),
      '用户A参与抽签'
    );
    ensureSuccess(
      await requestApi({
        method: 'POST',
        pathName: '/user/draw/join',
        token: userBToken,
        body: { drawId, shoeSize: firstSize, addressBookId: userBAddressId },
      }),
      '用户B参与抽签'
    );

    // 兜底触发开奖，若满员已自动开奖，此接口会幂等返回
    ensureSuccess(
      await requestApi({
        method: 'POST',
        pathName: `/admin/draw/manualDraw/${drawId}`,
        adminToken,
      }),
      '手动触发开奖'
    );

    const drawResult = await waitFor(
      async () => {
        const a = ensureSuccess(
          await requestApi({ pathName: `/user/draw/result/${drawId}`, token: userAToken }),
          '查询用户A抽签结果'
        );
        const b = ensureSuccess(
          await requestApi({ pathName: `/user/draw/result/${drawId}`, token: userBToken }),
          '查询用户B抽签结果'
        );
        if (![a?.status, b?.status].every((status) => status === 1 || status === 2)) {
          throw new Error(`开奖未完成: statusA=${a?.status}, statusB=${b?.status}`);
        }
        const winner = a.status === 1 ? 'A' : b.status === 1 ? 'B' : null;
        if (!winner) {
          throw new Error('未产生中奖者');
        }
        return { a, b, winner };
      },
      { timeoutMs: 25000, intervalMs: 1000, name: '等待开奖结果' }
    );

    const winnerToken = drawResult.winner === 'A' ? userAToken : userBToken;
    const winnerLabel = drawResult.winner;

    const winOptions = ensureSuccess(
      await requestApi({ pathName: `/user/draw/win/${drawId}/options`, token: winnerToken }),
      '查询中奖确认选项'
    );
    const winSize = winOptions?.shoeOptions?.[0]?.size || firstSize;

    const orderNo = ensureSuccess(
      await requestApi({
        method: 'POST',
        pathName: `/user/draw/win/${drawId}/confirm`,
        token: winnerToken,
        body: { shoeSize: winSize },
      }),
      '中奖确认下单'
    );
    if (!orderNo) {
      throw new Error('中奖确认未返回订单号');
    }

    const asyncStatus = await waitFor(
      async () => {
        const status = ensureSuccess(
          await requestApi({
            pathName: `/user/order/submit/status/${orderNo}`,
            token: winnerToken,
          }),
          '查询订单异步状态'
        );
        if (status?.status !== 'SUCCESS' || !status?.orderId) {
          throw new Error(`订单尚未成功: ${JSON.stringify(status)}`);
        }
        return status;
      },
      { timeoutMs: 25000, intervalMs: 1000, name: '等待抽签订单异步创建成功' }
    );

    ensureSuccess(
      await requestApi({
        method: 'PUT',
        pathName: '/user/order/payment',
        token: winnerToken,
        body: { orderNumber: orderNo, payMethod: 1 },
      }),
      '订单支付'
    );

    const orderDetail = ensureSuccess(
      await requestApi({
        pathName: `/user/order/orderDetail/${asyncStatus.orderId}`,
        token: winnerToken,
      }),
      '查询订单详情'
    );

    report.results.drawJoinRevealConfirm.ok = true;
    report.results.drawJoinRevealConfirm.details = `drawId=${drawId}, winner=${winnerLabel}, orderNo=${orderNo}, orderId=${asyncStatus.orderId}`;

    const unreadBefore = ensureSuccess(
      await requestApi({ pathName: '/user/profile/notifications/unread-count', token: winnerToken }),
      '查询未读消息数(前)'
    );

    const orderNotification = await waitFor(
      async () => {
        const list = ensureSuccess(
          await requestApi({
            pathName: '/user/profile/notifications',
            token: winnerToken,
            params: { type: 6, page: 1, size: 50 },
          }),
          '查询订单通知列表'
        );
        const found = list?.records?.find((item) => String(item?.content || '').includes(orderNo));
        if (!found) {
          throw new Error('订单通知尚未生成');
        }
        return found;
      },
      { timeoutMs: 20000, intervalMs: 1000, name: '等待订单通知生成' }
    );

    ensureSuccess(
      await requestApi({
        method: 'PUT',
        pathName: `/user/profile/notifications/${orderNotification.id}/read`,
        token: winnerToken,
      }),
      '订单通知标记已读'
    );

    const unreadAfter = ensureSuccess(
      await requestApi({ pathName: '/user/profile/notifications/unread-count', token: winnerToken }),
      '查询未读消息数(后)'
    );

    report.results.orderAndMessage.ok = true;
    report.results.orderAndMessage.details = `orderStatus=${orderDetail?.status}, unread=${unreadBefore}->${unreadAfter}, notificationId=${orderNotification.id}`;

    const experienceStart = new Date();
    const post = ensureSuccess(
      await requestApi({
        method: 'POST',
        pathName: '/user/experience/posts',
        token: userAToken,
        body: {
          content: `学生端联调心得 ${stamp}`,
          productType: 1,
          productId: spuList[0].id,
          images: [],
        },
      }),
      '发布心得'
    );
    const postId = post?.id;
    if (!postId) {
      throw new Error('发布心得未返回postId');
    }

    // 先尝试取消关注，避免重复关注导致无通知
    await requestApi({
      method: 'DELETE',
      pathName: `/user/experience/follow/${USER_A_ID}`,
      token: userBToken,
    });

    ensureSuccess(
      await requestApi({
        method: 'POST',
        pathName: `/user/experience/posts/${postId}/like`,
        token: userBToken,
      }),
      '点赞心得'
    );
    ensureSuccess(
      await requestApi({
        method: 'POST',
        pathName: `/user/experience/follow/${USER_A_ID}`,
        token: userBToken,
      }),
      '关注用户'
    );

    const likeNotification = await waitFor(
      async () => {
        const list = ensureSuccess(
          await requestApi({
            pathName: '/user/profile/notifications',
            token: userAToken,
            params: { type: 1, page: 1, size: 50 },
          }),
          '查询点赞通知'
        );
        const found = list?.records?.find((item) => item?.sourceUserId === USER_B_ID && item?.postId === postId);
        if (!found) {
          throw new Error('点赞通知尚未生成');
        }
        return found;
      },
      { timeoutMs: 20000, intervalMs: 1000, name: '等待点赞通知' }
    );

    const followNotification = await waitFor(
      async () => {
        const list = ensureSuccess(
          await requestApi({
            pathName: '/user/profile/notifications',
            token: userAToken,
            params: { type: 5, page: 1, size: 50 },
          }),
          '查询关注通知'
        );
        const found = list?.records?.find((item) => {
          if (item?.sourceUserId !== USER_B_ID) return false;
          const createdAt = item?.createTime ? new Date(item.createTime) : null;
          return createdAt && createdAt >= experienceStart;
        });
        if (!found) {
          throw new Error('关注通知尚未生成');
        }
        return found;
      },
      { timeoutMs: 20000, intervalMs: 1000, name: '等待关注通知' }
    );

    report.results.experience.ok = true;
    report.results.experience.details = `postId=${postId}, likeNotification=${likeNotification.id}, followNotification=${followNotification.id}`;

    const airdropTitle = `学生端联调空投-${stamp}`;
    const airdropStart = new Date(Date.now() - 60 * 1000);
    const airdropEnd = new Date(Date.now() + 10 * 60 * 1000);
    ensureSuccess(
      await requestApi({
        method: 'POST',
        pathName: '/admin/airdrop',
        adminToken,
        body: {
          title: airdropTitle,
          couponId: 1,
          totalCount: 1,
          remainCount: 1,
          startTime: formatDateTime(airdropStart),
          endTime: formatDateTime(airdropEnd),
          status: 1,
        },
      }),
      '创建联调空投活动'
    );

    const airdropPage = ensureSuccess(
      await requestApi({
        pathName: '/admin/airdrop/page',
        adminToken,
        params: { page: 1, pageSize: 20, title: airdropTitle },
      }),
      '查询联调空投活动'
    );
    const airdropId = airdropPage?.records?.find((item) => item.title === airdropTitle)?.id;
    if (!airdropId) {
      throw new Error('未查询到联调空投活动ID');
    }

    const [claimA, claimB] = await Promise.all([
      requestApi({ method: 'POST', pathName: `/user/airdrop/receive/${airdropId}`, token: userAToken }),
      requestApi({ method: 'POST', pathName: `/user/airdrop/receive/${airdropId}`, token: userBToken }),
    ]);

    const successUsers = [
      claimA.httpOk && claimA.bizOk ? 'A' : null,
      claimB.httpOk && claimB.bizOk ? 'B' : null,
    ].filter(Boolean);

    if (successUsers.length !== 1) {
      throw new Error(
        `空投并发领取结果异常: claimA(code=${claimA.code},msg=${claimA.msg}), claimB(code=${claimB.code},msg=${claimB.msg})`
      );
    }

    const airdropHistory = await waitFor(
      async () => {
        const historyA = ensureSuccess(
          await requestApi({
            pathName: '/user/profile/airdrops',
            token: userAToken,
            params: { page: 1, size: 20 },
          }),
          '查询用户A空投记录'
        );
        const historyB = ensureSuccess(
          await requestApi({
            pathName: '/user/profile/airdrops',
            token: userBToken,
            params: { page: 1, size: 20 },
          }),
          '查询用户B空投记录'
        );
        const gotA = !!historyA?.records?.find((item) => item.airdropId === airdropId);
        const gotB = !!historyB?.records?.find((item) => item.airdropId === airdropId);
        if (Number(gotA) + Number(gotB) !== 1) {
          throw new Error(`空投记录尚未稳定: gotA=${gotA}, gotB=${gotB}`);
        }
        return { gotA, gotB };
      },
      { timeoutMs: 25000, intervalMs: 1000, name: '等待空投记录落库' }
    );

    report.results.airdrop.ok = true;
    report.results.airdrop.details = `airdropId=${airdropId}, successUser=${successUsers[0]}, historyA=${airdropHistory.gotA}, historyB=${airdropHistory.gotB}`;
  } catch (error) {
    report.fatal = error instanceof Error ? error.message : String(error);
  } finally {
    report.finishedAt = new Date().toISOString();
    const reportPath = path.join(outDir, 'student-business-flow-report.json');
    await fs.writeFile(reportPath, JSON.stringify(report, null, 2), 'utf-8');
    console.log(JSON.stringify({ reportPath, outDir, report }, null, 2));
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
