import { useEffect, useRef, useCallback } from 'react';
import { useLocation } from 'react-router-dom';

const SCROLL_POSITION_KEY = 'scroll_positions';

function getScrollPositions(): Record<string, number> {
  try {
    const stored = sessionStorage.getItem(SCROLL_POSITION_KEY);
    return stored ? JSON.parse(stored) : {};
  } catch {
    return {};
  }
}

function saveScrollPosition(path: string, position: number) {
  try {
    const positions = getScrollPositions();
    positions[path] = position;
    sessionStorage.setItem(SCROLL_POSITION_KEY, JSON.stringify(positions));
  } catch (e) {
    console.error('Failed to save scroll position:', e);
  }
}

function getScrollPosition(path: string): number | null {
  const positions = getScrollPositions();
  return positions[path] ?? null;
}

function clearScrollPosition(path: string) {
  try {
    const positions = getScrollPositions();
    delete positions[path];
    sessionStorage.setItem(SCROLL_POSITION_KEY, JSON.stringify(positions));
  } catch (e) {
    console.error('Failed to clear scroll position:', e);
  }
}

if (typeof window !== 'undefined' && 'scrollRestoration' in history) {
  history.scrollRestoration = 'manual';
}

export function useScrollRestoration(enabled = true, dataLoaded = true) {
  const location = useLocation();
  const currentPathRef = useRef(location.pathname);
  const isRestoringRef = useRef(false);
  const restoredForPathRef = useRef<string | null>(null);
  const hasPendingRestoreRef = useRef(false);

  useEffect(() => {
    currentPathRef.current = location.pathname;
    
    const savedPosition = getScrollPosition(location.pathname);
    if (savedPosition !== null && savedPosition >= 0) {
      hasPendingRestoreRef.current = true;
      isRestoringRef.current = true;
    }
  }, [location.pathname]);

  const saveCurrentPosition = useCallback(() => {
    if (enabled && currentPathRef.current && !isRestoringRef.current) {
      saveScrollPosition(currentPathRef.current, window.scrollY);
    }
  }, [enabled]);

  useEffect(() => {
    if (!enabled) return;

    const path = location.pathname;
    const savedPosition = getScrollPosition(path);

    if (
      savedPosition !== null &&
      savedPosition >= 0 &&
      restoredForPathRef.current !== path &&
      dataLoaded
    ) {
      restoredForPathRef.current = path;
      isRestoringRef.current = true;

      const styleId = 'scroll-restoration-style';
      if (!document.getElementById(styleId)) {
        const style = document.createElement('style');
        style.id = styleId;
        style.textContent = `
          html.scroll-restoring { 
            overflow: hidden !important; 
          }
          html.scroll-restoring body {
            visibility: hidden !important;
          }
        `;
        document.head.appendChild(style);
      }

      document.documentElement.classList.add('scroll-restoring');

      let attempts = 0;
      const maxAttempts = 300;
      let rafId: number;
      let restored = false;

      const tryRestore = () => {
        if (restored || attempts >= maxAttempts) {
          if (!restored) {
            document.documentElement.classList.remove('scroll-restoring');
          }
          isRestoringRef.current = false;
          hasPendingRestoreRef.current = false;
          return;
        }
        attempts++;

        const docHeight = document.documentElement.scrollHeight;
        const winHeight = window.innerHeight;

        if (docHeight >= savedPosition + winHeight * 0.3) {
          window.scrollTo(0, savedPosition);
          document.documentElement.classList.remove('scroll-restoring');
          restored = true;
          isRestoringRef.current = false;
          hasPendingRestoreRef.current = false;
          return;
        }

        rafId = requestAnimationFrame(tryRestore);
      };

      rafId = requestAnimationFrame(tryRestore);

      const observer = new MutationObserver(() => {
        if (!restored) {
          const docHeight = document.documentElement.scrollHeight;
          const winHeight = window.innerHeight;

          if (docHeight >= savedPosition + winHeight * 0.3) {
            window.scrollTo(0, savedPosition);
            document.documentElement.classList.remove('scroll-restoring');
            restored = true;
            isRestoringRef.current = false;
            hasPendingRestoreRef.current = false;
            observer.disconnect();
          }
        }
      });

      observer.observe(document.body, {
        childList: true,
        subtree: true,
        attributes: false,
        characterData: false,
      });

      return () => {
        restored = true;
        cancelAnimationFrame(rafId);
        observer.disconnect();
        document.documentElement.classList.remove('scroll-restoring');
        isRestoringRef.current = false;
        hasPendingRestoreRef.current = false;
      };
    } else if (!hasPendingRestoreRef.current) {
      restoredForPathRef.current = path;
    }
  }, [enabled, location.pathname, dataLoaded]);

  useEffect(() => {
    if (!enabled) return;

    const path = location.pathname;

    const throttledSave = throttle(() => {
      if (!isRestoringRef.current && !hasPendingRestoreRef.current) {
        saveScrollPosition(path, window.scrollY);
      }
    }, 100);

    window.addEventListener('scroll', throttledSave, { passive: true });

    const handleClick = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      const link = target.closest('a');
      if (link && link.href && !link.href.startsWith('javascript:') && !isRestoringRef.current) {
        saveScrollPosition(path, window.scrollY);
      }
    };

    document.addEventListener('click', handleClick, true);

    return () => {
      window.removeEventListener('scroll', throttledSave);
      document.removeEventListener('click', handleClick, true);
    };
  }, [enabled, location.pathname]);

  return {
    clearScrollPosition: () => clearScrollPosition(currentPathRef.current),
    saveCurrentPosition,
  };
}

function throttle<T extends (...args: any[]) => void>(fn: T, delay: number): T {
  let lastTime = 0;
  return ((...args: Parameters<T>) => {
    const now = Date.now();
    if (now - lastTime >= delay) {
      lastTime = now;
      fn(...args);
    }
  }) as T;
}
