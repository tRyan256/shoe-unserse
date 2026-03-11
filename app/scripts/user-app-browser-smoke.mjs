import { chromium } from 'playwright';
import fs from 'node:fs/promises';
import path from 'node:path';

const BASE_URL = process.env.BASE_URL || 'http://127.0.0.1:4173';
const PHONE = process.env.LOGIN_PHONE || '13800138006';
const CODE = process.env.LOGIN_CODE || '149932';

const now = new Date();
const stamp = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}-${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}${String(now.getSeconds()).padStart(2, '0')}`;
const outDir = path.resolve('test-screenshots', `user-app-smoke-${stamp}`);

function parseApiPayload(raw) {
  if (!raw) return null;
  if (typeof raw === 'object') return raw;
  try {
    return JSON.parse(raw);
  } catch {
    return { raw };
  }
}

async function main() {
  await fs.mkdir(outDir, { recursive: true });

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 390, height: 844 },
    locale: 'zh-CN',
  });
  const page = await context.newPage();

  const pageErrors = [];
  page.on('pageerror', (err) => {
    pageErrors.push({ url: page.url(), message: err.message });
  });

  const report = {
    startedAt: new Date().toISOString(),
    baseUrl: BASE_URL,
    account: PHONE,
    pages: [],
    pageErrors: [],
    summary: {
      total: 0,
      passed: 0,
      failed: 0,
    },
  };

  const shot = async (name) => {
    const filePath = path.join(outDir, `${name}.png`);
    await page.screenshot({ path: filePath, fullPage: true });
    return filePath;
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

  try {
    await page.goto(`${BASE_URL}/login`, { waitUntil: 'domcontentloaded' });
    await page.getByPlaceholder('请输入手机号').fill(PHONE);
    await page.getByPlaceholder('请输入验证码').fill(CODE);
    await page.getByRole('button', { name: /^登录$/ }).click();
    await page.waitForURL(/\/shop|\/$/, { timeout: 15000 });

    const token = await getToken();
    if (!token) {
      throw new Error('登录成功后未获取到 token');
    }

    const shoeResp = await api(token, '/user/shoe/list');
    const shoeId = Array.isArray(shoeResp.payload?.data) && shoeResp.payload.data.length > 0
      ? Number(shoeResp.payload.data[0].id)
      : null;

    const bundleResp = await api(token, '/user/bundle/list');
    const bundleId = Array.isArray(bundleResp.payload?.data) && bundleResp.payload.data.length > 0
      ? Number(bundleResp.payload.data[0].id)
      : null;

    const drawResp = await api(token, '/user/draw/list');
    const drawId = Array.isArray(drawResp.payload?.data) && drawResp.payload.data.length > 0
      ? Number(drawResp.payload.data[0].id)
      : null;

    const orderResp = await api(token, '/user/order/historyOrders?page=1&pageSize=10');
    const orderId = orderResp.payload?.data?.records?.[0]?.id ? Number(orderResp.payload.data.records[0].id) : null;

    const postResp = await api(token, '/user/experience/posts/all?page=1&pageSize=10');
    const postId = postResp.payload?.data?.records?.[0]?.id ? Number(postResp.payload.data.records[0].id) : null;

    const routes = [
      { name: 'shop', path: '/shop' },
      { name: 'category', path: '/category' },
      { name: 'community', path: '/community' },
      { name: 'activity', path: '/activity' },
      { name: 'draw-list', path: '/draw/list' },
      { name: 'draw-my', path: '/draw/my' },
      { name: 'cart', path: '/cart' },
      { name: 'order-list', path: '/order/list' },
      { name: 'message', path: '/message' },
      { name: 'message-center', path: '/user/message-center' },
      { name: 'user-home', path: '/user' },
      { name: 'user-address', path: '/user/address' },
      { name: 'user-coupon', path: '/user/coupon' },
      { name: 'user-draw-history', path: '/user/draw-history' },
      { name: 'user-airdrop-history', path: '/user/airdrop-history' },
      { name: 'coupon-my', path: '/coupon/my' },
      { name: 'outlet-nearby', path: '/outlet/nearby' },
      { name: 'settings', path: '/settings' },
      { name: 'agreement', path: '/agreement' },
      { name: 'help', path: '/help' },
    ];

    if (shoeId) routes.push({ name: 'shoe-detail', path: `/shoe/${shoeId}` });
    if (bundleId) routes.push({ name: 'bundle-detail', path: `/bundle/${bundleId}` });
    if (drawId) routes.push({ name: 'draw-detail', path: `/draw/detail/${drawId}` });
    if (drawId) routes.push({ name: 'draw-history-detail', path: `/user/draw-history/detail/${drawId}` });
    if (orderId) routes.push({ name: 'order-detail', path: `/order/detail/${orderId}` });
    if (postId) routes.push({ name: 'community-detail', path: `/community/detail/${postId}` });

    let index = 1;
    for (const route of routes) {
      try {
        await page.goto(`${BASE_URL}${route.path}`, { waitUntil: 'domcontentloaded' });
        await page.waitForTimeout(1200);

        const redirectedToLogin = /\/login/.test(page.url());
        const screenshot = await shot(`${String(index).padStart(2, '0')}-${route.name}`);
        const ok = !redirectedToLogin;

        report.pages.push({
          name: route.name,
          path: route.path,
          ok,
          url: page.url(),
          screenshot,
          details: ok ? '页面可访问' : '页面跳转到登录页',
        });
      } catch (error) {
        const screenshot = await shot(`${String(index).padStart(2, '0')}-${route.name}-error`);
        report.pages.push({
          name: route.name,
          path: route.path,
          ok: false,
          url: page.url(),
          screenshot,
          details: `异常: ${error instanceof Error ? error.message : String(error)}`,
        });
      }
      index += 1;
    }

    report.pageErrors = pageErrors;
    report.summary.total = report.pages.length;
    report.summary.passed = report.pages.filter((p) => p.ok).length;
    report.summary.failed = report.pages.filter((p) => !p.ok).length;
  } finally {
    report.finishedAt = new Date().toISOString();
    const reportPath = path.join(outDir, 'user-app-smoke-report.json');
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
