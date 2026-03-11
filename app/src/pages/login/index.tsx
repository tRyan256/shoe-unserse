import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { ArrowLeft, Phone, Shield } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useUserStore } from '@/stores';
import { userApi } from '@/api';
import { isValidPhone, isValidCode } from '@/utils';
import { APP_NAME, CODE_COUNTDOWN } from '@/constants';

export default function LoginPage() {
  const navigate = useNavigate();
  const { setUser, setToken } = useUserStore();
  const [phone, setPhone] = useState('');
  const [code, setCode] = useState('');
  const [countdown, setCountdown] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (countdown > 0) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [countdown]);

  const handleSendCode = async () => {
    if (!isValidPhone(phone)) {
      setError('请输入正确的手机号');
      return;
    }
    setError('');
    try {
      await userApi.sendCode(phone);
      setCountdown(CODE_COUNTDOWN);
    } catch {
      setError('发送验证码失败，请重试');
    }
  };

  const handleLogin = async () => {
    if (!isValidPhone(phone)) {
      setError('请输入正确的手机号');
      return;
    }
    if (!isValidCode(code)) {
      setError('请输入6位验证码');
      return;
    }

    setError('');
    setIsLoading(true);
    try {
      const res = await userApi.login(phone, code);
      setToken(res.token);
      setUser({ id: String(res.id), phone: res.phone, nickname: '', avatar: '' });
      navigate('/', { replace: true });
    } catch {
      setError('登录失败，请检查验证码');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen overflow-hidden bg-gradient-to-b from-[#fff9ef] via-[#f8f2e8] to-[#f2ece0]">
      <div className="pointer-events-none absolute inset-0">
        <div className="absolute -left-12 -top-16 h-56 w-56 rounded-full bg-amber-300/35 blur-3xl" />
        <div className="absolute -right-16 top-10 h-64 w-64 rounded-full bg-emerald-300/25 blur-3xl" />
        <div className="absolute bottom-0 left-1/2 h-52 w-72 -translate-x-1/2 rounded-full bg-orange-200/25 blur-3xl" />
      </div>

      <div className="relative z-10 flex min-h-screen flex-col">
        <motion.header
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="flex h-14 items-center px-4"
        >
          <button
            onClick={() => navigate(-1)}
            className="flex h-10 w-10 items-center justify-center rounded-full border border-amber-200 bg-white/85 text-stone-700"
          >
            <ArrowLeft className="h-5 w-5" />
          </button>
        </motion.header>

        <div className="flex-1 px-5 pt-3">
          <motion.div
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.08 }}
            className="mb-8 flex flex-col items-center"
          >
            <div className="mb-4 flex h-24 w-24 items-center justify-center rounded-full border border-amber-100 bg-white shadow-[0_8px_30px_rgba(142,107,60,0.18)]">
              <img src="/logo.png" alt={APP_NAME} className="h-16 w-16 object-contain" />
            </div>
            <h1 className="text-[30px] font-bold tracking-[0.01em] text-stone-900">欢迎回来</h1>
            <p className="mt-1 text-sm text-stone-500">登录{APP_NAME}，开启球鞋之旅</p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.16 }}
            className="rounded-3xl border border-amber-100/80 bg-white/90 p-5 shadow-[0_14px_36px_rgba(127,96,53,0.16)] backdrop-blur"
          >
            <div className="space-y-4">
              <div className="space-y-1.5">
                <label className="text-[13px] font-semibold text-stone-600">手机号</label>
                <div className="relative">
                  <Phone className="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-stone-400" />
                  <Input
                    ref={inputRef}
                    type="tel"
                    placeholder="请输入手机号"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value.replace(/\D/g, '').slice(0, 11))}
                    className="h-12 rounded-xl border-amber-100 bg-amber-50/35 pl-11 text-sm text-stone-800 placeholder:text-stone-400 focus-visible:ring-amber-300"
                    maxLength={11}
                  />
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-[13px] font-semibold text-stone-600">验证码</label>
                <div className="flex gap-2.5">
                  <div className="relative flex-1">
                    <Shield className="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-stone-400" />
                    <Input
                      type="number"
                      placeholder="请输入验证码"
                      value={code}
                      onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                      className="h-12 rounded-xl border-amber-100 bg-amber-50/35 pl-11 text-sm text-stone-800 placeholder:text-stone-400 focus-visible:ring-amber-300"
                      maxLength={6}
                    />
                  </div>
                  <Button
                    variant="outline"
                    className="h-12 min-w-[92px] rounded-xl border-amber-200 bg-white text-xs text-amber-700 hover:bg-amber-50 hover:text-amber-800"
                    onClick={handleSendCode}
                    disabled={countdown > 0 || !isValidPhone(phone)}
                  >
                    {countdown > 0 ? `${countdown}s` : '获取验证码'}
                  </Button>
                </div>
              </div>

              <AnimatePresence>
                {error && (
                  <motion.p
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    exit={{ opacity: 0, height: 0 }}
                    className="text-xs font-medium text-red-500"
                  >
                    {error}
                  </motion.p>
                )}
              </AnimatePresence>

              <Button
                className="h-12 w-full rounded-xl bg-gradient-to-r from-amber-500 to-orange-500 text-white shadow-[0_10px_24px_rgba(214,130,39,0.32)] hover:from-amber-600 hover:to-orange-600"
                onClick={handleLogin}
                disabled={isLoading || !isValidPhone(phone) || !isValidCode(code)}
              >
                {isLoading ? '登录中...' : '登录'}
              </Button>

              <p className="text-center text-xs text-stone-400">未注册的手机号将自动创建账号</p>
            </div>
          </motion.div>
        </div>

        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.3 }}
          className="px-6 py-8 text-center"
        >
          <p className="text-[11px] text-stone-400">登录即表示同意《用户协议》和《隐私政策》</p>
        </motion.div>
      </div>
    </div>
  );
}
