import { chromium } from 'playwright';
import crypto from 'node:crypto';
import fs from 'node:fs/promises';
import path from 'node:path';

const BASE_URL = 'http://127.0.0.1:4173';
const PHONE = '13800138006';
const CODE = process.env.LOGIN_CODE || '149932';
const USER_ID = Number(process.env.LOGIN_USER_ID || 6);
const JWT_SECRET = process.env.JWT_USER_SECRET || 'suwechat';
const JWT_TTL_MS = Number(process.env.JWT_TTL_MS || 30 * 24 * 60 * 60 * 1000);
const ALLOW_LOGIN_FALLBACK = process.env.ALLOW_LOGIN_FALLBACK === '1';
const BASE_API = process.env.BASE_API || 'http://127.0.0.1:8080';
const ADMIN_ID = Number(process.env.ADMIN_ID || 1);
const ADMIN_SECRET = process.env.JWT_ADMIN_SECRET || 'itcast';

const now = new Date();
const stamp = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}-${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}${String(now.getSeconds()).padStart(2, '0')}`;
const outDir = path.resolve('test-screenshots', `regression-${stamp}`);

function parseApiPayload(raw) {
  if (!raw) return null;
  if (typeof raw === 'object') return raw;
  try {
    return JSON.parse(raw);
  } catch {
    return { raw };
  }
}

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
  const sig = crypto
    .createHmac('sha256', getSecretKeyBytes(secret))
    .update(signingInput)
    .digest('base64url');
  return `${signingInput}.${sig}`;
}

function createUserJwt(userId, secret, ttlMs) {
  return createJwt('userId', userId, secret, ttlMs);
}

function createAdminJwt(adminId, secret, ttlMs) {
  return createJwt('empId', adminId, secret, ttlMs);
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

function createPersistedUserStorage(token, phone, userId) {
  return JSON.stringify({
    state: {
      user: {
        id: String(userId),
        phone,
        nickname: '',
        avatar: '',
      },
      token,
    },
    version: 0,
  });
}

async function main() {
  await fs.mkdir(outDir, { recursive: true });

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 390, height: 844 },
    locale: 'zh-CN',
  });
  const page = await context.newPage();

  const report = {
    startedAt: new Date().toISOString(),
    baseUrl: BASE_URL,
    account: PHONE,
    codeUsed: CODE,
    results: {
      login: { ok: false, details: '' },
      orderSubmit: { ok: false, details: '' },
      drawWinConfirm: { ok: false, details: '' },
      airdropClaim: { ok: false, details: '' },
      messageRead: { ok: false, details: '', unreadBefore: null, unreadAfter: null },
    },
    artifacts: [],
  };

  const shot = async (name, fullPage = true) => {
    const filePath = path.join(outDir, `${name}.png`);
    await page.screenshot({ path: filePath, fullPage });
    report.artifacts.push(filePath);
  };

  const getToken = async () =>
    page.evaluate(() => {
      const raw = localStorage.getItem('user-storage');
      if (!raw) return null;
      try {
        const parsed = JSON.parse(raw);
        return parsed?.state?.token || null;
      } catch {
        return null;
      }
    });

  const latestToastText = async () => {
    const selectors = ['[data-sonner-toast]', '[data-testid=\"toast\"]', '[role=\"status\"]'];
    for (const selector of selectors) {
      const locator = page.locator(selector).last();
      if ((await locator.count()) === 0) continue;
      const txt = (await locator.textContent())?.trim();
      if (txt) return txt;
    }
    return null;
  };

  const api = async (token, pathName, method = 'GET', body) => {
    const result = await page.evaluate(
      async ({ token, pathName, method, body }) => {
        const resp = await fetch(`/api${pathName}`, {
          method,
          headers: {
            'Content-Type': 'application/json',
            Authorization: token || '',
          },
          body: body ? JSON.stringify(body) : undefined,
        });
        const text = await resp.text();
        return { status: resp.status, ok: resp.ok, text };
      },
      { token, pathName, method, body }
    );

    return {
      ...result,
      payload: parseApiPayload(result.text),
    };
  };

  const adminToken = createAdminJwt(ADMIN_ID, ADMIN_SECRET, JWT_TTL_MS);

  const adminApi = async (pathName, method = 'GET', body) => {
    const response = await fetch(`${BASE_API}${pathName}`, {
      method,
      headers: {
        'Content-Type': 'application/json',
        token: adminToken,
      },
      body: body ? JSON.stringify(body) : undefined,
    });
    const text = await response.text();
    const payload = parseApiPayload(text);
    return {
      status: response.status,
      ok: response.ok,
      payload,
      text,
    };
  };

  const ensureAdminSuccess = (response, label) => {
    if (response.ok && response.payload?.code === 1) {
      return response.payload?.data;
    }
    throw new Error(
      `${label}失败: HTTP=${response.status}, code=${response.payload?.code ?? 'N/A'}, msg=${response.payload?.msg || response.text || '未知错误'}`
    );
  };

  try {
    // 1) Login with UI; fallback to JWT injection for deterministic regression.
    await page.goto(`${BASE_URL}/login`, { waitUntil: 'domcontentloaded' });
    await page.getByPlaceholder('请输入手机号').fill(PHONE);
    await page.getByPlaceholder('请输入验证码').fill(CODE);
    await page.getByRole('button', { name: /^登录$/ }).click();

    let loginByFallback = false;
    try {
      await page.waitForURL(/\/shop|\/$/, { timeout: 12000 });
    } catch {
      loginByFallback = true;
    }

    if (loginByFallback || /\/login/.test(page.url())) {
      if (!ALLOW_LOGIN_FALLBACK) {
        const toast = await latestToastText();
        await shot('01-login-fail');
        throw new Error(toast ? `验证码登录失败: ${toast}` : '验证码登录失败');
      }
      const fallbackToken = createUserJwt(USER_ID, JWT_SECRET, JWT_TTL_MS);
      const storageRaw = createPersistedUserStorage(fallbackToken, PHONE, USER_ID);
      await page.evaluate(({ storageRaw }) => localStorage.setItem('user-storage', storageRaw), { storageRaw });
      await page.goto(`${BASE_URL}/shop`, { waitUntil: 'domcontentloaded' });
      report.results.login.details = '验证码登录失败，已使用同账号JWT注入兜底';
    } else {
      report.results.login.details = `登录成功，当前URL: ${page.url()}`;
    }

    await shot('01-login-success');
    report.results.login.ok = true;

    const token = await getToken();
    if (!token) {
      throw new Error('登录后未获取到 token');
    }

    // 2) Prepare address + cart item for order flow
    const addressList = await api(token, '/user/addressBook/list');
    const addresses = addressList.payload?.data || [];
    if (!Array.isArray(addresses) || addresses.length === 0) {
      await api(token, '/user/addressBook', 'POST', {
        consignee: '孙八',
        phone: PHONE,
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
      });
    }

    await api(token, '/user/shoppingCart/clean', 'DELETE');

    const spuListResp = await api(token, '/user/shoe/list');
    const spuList = spuListResp.payload?.data || [];
    if (!Array.isArray(spuList) || spuList.length === 0) {
      throw new Error('无法获取鞋款列表，无法执行下单回归');
    }
    const firstSpu = spuList[0];

    const spuDetailResp = await api(token, `/user/shoe/${firstSpu.id}`);
    const spuDetail = spuDetailResp.payload?.data;
    const firstSku = spuDetail?.skus?.[0];
    const firstSize = firstSku?.sizes?.[0]?.size;
    if (!firstSku?.id || !firstSize) {
      throw new Error('无法获取 SKU 或尺码，无法执行下单回归');
    }

    await api(token, '/user/shoppingCart/add', 'POST', {
      spuId: spuDetail.id,
      skuId: firstSku.id,
      shoeSize: firstSize,
      selected: 1,
    });

    // 3) Order submit flow
    try {
      await page.goto(`${BASE_URL}/cart`, { waitUntil: 'domcontentloaded' });
      await page.waitForTimeout(1200);

      const checkoutBtn = page.getByRole('button', { name: /结算/ }).first();
      if (await checkoutBtn.isDisabled()) {
        const selectAllText = page.getByText('全选').first();
        if (await selectAllText.count()) {
          await selectAllText.click();
          await page.waitForTimeout(400);
        }
      }

      await checkoutBtn.scrollIntoViewIfNeeded();
      await checkoutBtn.click();
      await page.waitForTimeout(1200);
      const submitOrderBtn = page.getByRole('button', { name: /提交订单/ }).first();
      if ((await submitOrderBtn.count()) === 0) {
        await shot('02-order-confirm-fail');
        throw new Error(`未进入确认订单页，当前URL: ${page.url()}`);
      }
      await shot('02-order-confirm');
      await submitOrderBtn.click();

      try {
        await page.waitForURL(/\/order\/payment/, { timeout: 40000 });
        await page.waitForTimeout(1000);
        if (!/\/order\/payment/.test(page.url())) {
          throw new Error(`支付页未稳定停留，当前URL: ${page.url()}`);
        }
        await shot('03-order-submit-success');
        report.results.orderSubmit.ok = true;
        report.results.orderSubmit.details = `下单成功，跳转到支付页: ${page.url()}`;
      } catch {
        await shot('03-order-submit-fail');
        const toast = await latestToastText();
        const errText = await page
          .locator('text=订单提交失败')
          .first()
          .textContent()
          .catch(() => null);
        report.results.orderSubmit.ok = false;
        report.results.orderSubmit.details =
          toast || errText || `未跳转支付页，当前URL: ${page.url()}`;
      }
    } catch (e) {
      await shot('03-order-submit-exception');
      report.results.orderSubmit.ok = false;
      report.results.orderSubmit.details = `异常: ${e instanceof Error ? e.message : String(e)}`;
    }

    // 4) Draw win confirm flow
    try {
      let targetDrawId = null;
      const profileDrawResp = await api(token, '/user/profile/draws?page=1&size=50');
      const drawRecords = profileDrawResp.payload?.data?.records || [];
      if (Array.isArray(drawRecords)) {
        for (const record of drawRecords) {
          if (!record?.drawId || record?.status !== 1 || record?.orderNo) continue;
          const optionsResp = await api(token, `/user/draw/win/${record.drawId}/options`);
          const shoeOptions = optionsResp.payload?.data?.shoeOptions || [];
          if (optionsResp.payload?.code === 1 && Array.isArray(shoeOptions) && shoeOptions.length > 0) {
            targetDrawId = Number(record.drawId);
            break;
          }
        }
      }

      if (!targetDrawId) {
        const addressResp = await api(token, '/user/addressBook/list');
        const addressList = addressResp.payload?.data || [];
        const addressBookId = Array.isArray(addressList) && addressList.length > 0 ? Number(addressList[0].id) : null;
        if (!addressBookId) {
          throw new Error('未找到可用地址，无法构造中签确认回归数据');
        }

        const drawTitle = `浏览器回归中签-${stamp}`;
        const drawStart = new Date(Date.now() - 60 * 1000);
        const drawEnd = new Date(Date.now() + 10 * 60 * 1000);

        ensureAdminSuccess(
          await adminApi('/admin/draw', 'POST', {
            title: drawTitle,
            targetType: 1,
            skuId: firstSku.id,
            totalStock: 2,
            maxParticipants: 2,
            winnerCount: 1,
            price: Number(firstSku.price || 0),
            startTime: formatDateTime(drawStart),
            endTime: formatDateTime(drawEnd),
            status: 1,
            description: '浏览器回归自动构造中签确认数据',
          }),
          '创建回归抽签活动'
        );

        const drawPage = ensureAdminSuccess(
          await adminApi(`/admin/draw/page?page=1&pageSize=20&title=${encodeURIComponent(drawTitle)}`),
          '查询回归抽签活动'
        );
        const seededDrawId = drawPage?.records?.find((item) => item.title === drawTitle)?.id;
        if (!seededDrawId) {
          throw new Error('创建回归抽签后未查到抽签ID');
        }

        const joinResp = await api(token, '/user/draw/join', 'POST', {
          drawId: Number(seededDrawId),
          shoeSize: firstSize,
          addressBookId,
        });
        if (joinResp.payload?.code !== 1) {
          throw new Error(joinResp.payload?.msg || '参与回归抽签失败');
        }

        ensureAdminSuccess(await adminApi(`/admin/draw/manualDraw/${seededDrawId}`, 'POST'), '执行手动开奖');
        targetDrawId = Number(seededDrawId);
        await page.waitForTimeout(1000);
      }

      await page.goto(`${BASE_URL}/draw/win-confirm/${targetDrawId}`, { waitUntil: 'domcontentloaded' });
      await page.waitForSelector('text=中奖确认', { timeout: 15000 });
      await shot('04-draw-win-confirm-page');

      await page.getByRole('button', { name: '确认购买' }).click();
      await page.waitForTimeout(2500);

      if (/\/order\/list/.test(page.url())) {
        await shot('05-draw-win-confirm-success');
        report.results.drawWinConfirm.ok = true;
        report.results.drawWinConfirm.details = `中签确认成功，drawId=${targetDrawId}，跳转到订单列表: ${page.url()}`;
      } else {
        await shot('05-draw-win-confirm-fail');
        const toast = await latestToastText();
        report.results.drawWinConfirm.ok = false;
        report.results.drawWinConfirm.details =
          toast || `未跳转订单列表，drawId=${targetDrawId}，当前URL: ${page.url()}`;
      }
    } catch (e) {
      await shot('05-draw-win-confirm-exception');
      report.results.drawWinConfirm.ok = false;
      report.results.drawWinConfirm.details = `异常: ${e instanceof Error ? e.message : String(e)}`;
    }


    // 5) Airdrop claim flow
    try {
      const airdropTitle = `浏览器回归空投-${stamp}`;
      const airdropStart = new Date(Date.now() - 60 * 1000);
      const airdropEnd = new Date(Date.now() + 10 * 60 * 1000);

      ensureAdminSuccess(
        await adminApi('/admin/airdrop', 'POST', {
          title: airdropTitle,
          couponId: 1,
          totalCount: 1,
          remainCount: 1,
          startTime: formatDateTime(airdropStart),
          endTime: formatDateTime(airdropEnd),
          status: 1,
        }),
        '创建回归空投活动'
      );

      const airdropPage = ensureAdminSuccess(
        await adminApi(`/admin/airdrop/page?page=1&pageSize=20&title=${encodeURIComponent(airdropTitle)}`),
        '查询回归空投活动'
      );
      const targetAirdropId = airdropPage?.records?.find((item) => item.title === airdropTitle)?.id;
      if (!targetAirdropId) {
        throw new Error('创建回归空投后未查到活动ID');
      }

      await page.goto(`${BASE_URL}/activity?tab=airdrop`, { waitUntil: 'domcontentloaded' });
      await page.waitForTimeout(1800);

      const airdropCard = page
        .locator('div.bg-white.rounded-xl.p-4.shadow-sm')
        .filter({ hasText: airdropTitle })
        .first();
      if ((await airdropCard.count()) === 0) {
        throw new Error('活动页未渲染目标空投卡片');
      }

      const claimBtn = airdropCard.getByRole('button', { name: '领取' }).first();
      if ((await claimBtn.count()) === 0 || (await claimBtn.isDisabled())) {
        throw new Error('未找到可点击的领取按钮');
      }
      await claimBtn.click();
      await page.waitForTimeout(1500);

      await shot('06-airdrop-claim');

      const historyResp = await api(token, '/user/profile/airdrops?page=1&size=20');
      const historyRecords = historyResp.payload?.data?.records || [];
      const received = Array.isArray(historyRecords)
        ? historyRecords.some((item) => Number(item.airdropId) === Number(targetAirdropId))
        : false;

      if (received) {
        report.results.airdropClaim.ok = true;
        report.results.airdropClaim.details = `空投领取成功，airdropId=${targetAirdropId}`;
      } else {
        const toast = await latestToastText();
        report.results.airdropClaim.ok = false;
        report.results.airdropClaim.details = toast || `空投领取后未在历史记录找到，airdropId=${targetAirdropId}`;
      }
    } catch (e) {
      await shot('06-airdrop-claim-exception');
      report.results.airdropClaim.ok = false;
      report.results.airdropClaim.details = `异常: ${e instanceof Error ? e.message : String(e)}`;
    }
    // 6) Message read flow
    try {
      await page.goto(`${BASE_URL}/user/message-center`, { waitUntil: 'domcontentloaded' });
      await page.waitForTimeout(1500);
      await shot('07-message-before');

      const unreadLocator = page.locator('div.border-l-2');
      const unreadBefore = await unreadLocator.count();
      report.results.messageRead.unreadBefore = unreadBefore;

      if (unreadBefore > 0) {
        await unreadLocator.first().click();
        await page.waitForTimeout(1500);
      }

      const unreadAfter = await unreadLocator.count();
      report.results.messageRead.unreadAfter = unreadAfter;
      await shot('08-message-after');

      if (unreadBefore > unreadAfter) {
        report.results.messageRead.ok = true;
        report.results.messageRead.details = `未读数从 ${unreadBefore} -> ${unreadAfter}`;
      } else if (unreadBefore === 0) {
        report.results.messageRead.ok = true;
        report.results.messageRead.details = '进入消息中心时无未读消息';
      } else {
        report.results.messageRead.ok = false;
        report.results.messageRead.details = `点击后未读数未下降: ${unreadBefore} -> ${unreadAfter}`;
      }
    } catch (e) {
      await shot('08-message-exception');
      report.results.messageRead.ok = false;
      report.results.messageRead.details = `异常: ${e instanceof Error ? e.message : String(e)}`;
    }
  } catch (e) {
    report.fatal = e instanceof Error ? e.message : String(e);
  } finally {
    report.finishedAt = new Date().toISOString();
    const reportPath = path.join(outDir, 'regression-report.json');
    await fs.writeFile(reportPath, JSON.stringify(report, null, 2), 'utf-8');
    await context.close();
    await browser.close();
    console.log(JSON.stringify({ reportPath, outDir, report }, null, 2));
  }
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});








