import { useEffect, useRef } from 'react';

const DEFAULT_POLLING_INTERVAL_MS = 30000;

/*
 * 관리자 MIS 조건부 폴링 훅.
 * 화면이 보이는 동안만 30초 단위로 최신 데이터를 재조회하고,
 * 탭이 다시 활성화되면 즉시 한 번 재조회한다.
 */
const useAdminPolling = (callback, options = {}) => {
  const {
    enabled = true,
    intervalMs = DEFAULT_POLLING_INTERVAL_MS,
    label = 'admin-polling',
  } = options;

  const callbackRef = useRef(callback);
  const runningRef = useRef(false);

  useEffect(() => {
    callbackRef.current = callback;
  }, [callback]);

  useEffect(() => {
    if (!enabled) {
      console.log('관리자 폴링 비활성화', label);
      return undefined;
    }

    let timerId;

    const runPolling = async (reason) => {
      if (document.visibilityState !== 'visible') {
        console.log('관리자 폴링 중지 상태', label, reason);
        return;
      }

      if (runningRef.current) {
        console.log('관리자 폴링 중복 실행 방지', label, reason);
        return;
      }

      try {
        runningRef.current = true;
        console.log('관리자 조건부 폴링 실행', label, reason);
        await callbackRef.current?.();
      } catch (error) {
        console.log('관리자 조건부 폴링 실패', label, error);
      } finally {
        runningRef.current = false;
      }
    };

    const startTimer = () => {
      if (timerId) {
        clearInterval(timerId);
      }

      if (document.visibilityState !== 'visible') {
        console.log('관리자 폴링 타이머 시작 보류', label);
        return;
      }

      console.log('관리자 폴링 타이머 시작', label, intervalMs);
      timerId = setInterval(() => runPolling('interval'), intervalMs);
    };

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        console.log('관리자 탭 활성화 폴링 재시작', label);
        runPolling('tab-visible');
        startTimer();
        return;
      }

      console.log('관리자 탭 비활성화 폴링 중지', label);
      if (timerId) {
        clearInterval(timerId);
        timerId = undefined;
      }
    };

    startTimer();
    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      console.log('관리자 폴링 해제', label);
      if (timerId) {
        clearInterval(timerId);
      }
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [enabled, intervalMs, label]);
};

export default useAdminPolling;
