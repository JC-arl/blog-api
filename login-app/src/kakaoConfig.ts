// Kakao REST API 직접 사용 방식
const KAKAO_REST_API_KEY = process.env.REACT_APP_KAKAO_REST_API_KEY;
// 개발/운영 모두 8080 포트 사용 (Spring Boot에서 React 빌드 서빙)
const KAKAO_REDIRECT_URI = 'http://localhost:8080/oauth/kakao/callback';

// 카카오 로그인 - 팝업 방식
export const kakaoLogin = (): Promise<string> => {
    return new Promise((resolve, reject) => {
        // 디버깅: 키와 URI 확인
        console.log('=== Kakao Login Debug ===');
        console.log('REST API Key:', KAKAO_REST_API_KEY);
        console.log('Redirect URI:', KAKAO_REDIRECT_URI);

        if (!KAKAO_REST_API_KEY) {
            reject(new Error('카카오 REST API 키가 설정되지 않았습니다. .env 파일을 확인하세요.'));
            return;
        }

        // scope: 프로필 닉네임만 요청 (무료 권한)
        const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_REST_API_KEY}&redirect_uri=${encodeURIComponent(KAKAO_REDIRECT_URI)}&response_type=code&scope=profile_nickname`;

        console.log('요청 URL:', kakaoAuthUrl);
        console.log('=======================');

        const width = 500;
        const height = 600;
        const left = window.screen.width / 2 - width / 2;
        const top = window.screen.height / 2 - height / 2;

        const popup = window.open(
            kakaoAuthUrl,
            'Kakao Login',
            `width=${width},height=${height},left=${left},top=${top}`
        );

        if (!popup) {
            reject(new Error('팝업이 차단되었습니다. 팝업 차단을 해제해주세요.'));
            return;
        }

        // 팝업에서 인가 코드를 받기 위한 이벤트 리스너
        const handleMessage = async (event: MessageEvent) => {
            // 보안: origin 확인
            if (event.origin !== window.location.origin) {
                return;
            }

            if (event.data.type === 'KAKAO_AUTH_CODE') {
                window.removeEventListener('message', handleMessage);

                const authCode = event.data.code;

                if (authCode) {
                    try {
                        // 인가 코드를 액세스 토큰으로 교환
                        const tokenResponse = await fetch('https://kauth.kakao.com/oauth/token', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/x-www-form-urlencoded',
                            },
                            body: new URLSearchParams({
                                grant_type: 'authorization_code',
                                client_id: KAKAO_REST_API_KEY!,
                                redirect_uri: KAKAO_REDIRECT_URI,
                                code: authCode,
                            }),
                        });

                        if (!tokenResponse.ok) {
                            throw new Error('토큰 교환 실패');
                        }

                        const tokenData = await tokenResponse.json();
                        resolve(tokenData.access_token);
                    } catch (error) {
                        reject(error);
                    }
                } else {
                    reject(new Error('인가 코드를 받지 못했습니다.'));
                }
            } else if (event.data.type === 'KAKAO_AUTH_ERROR') {
                window.removeEventListener('message', handleMessage);
                reject(new Error(event.data.error || '카카오 로그인 실패'));
            }
        };

        window.addEventListener('message', handleMessage);

        // 팝업이 닫혔는지 확인
        const checkPopupClosed = setInterval(() => {
            if (popup.closed) {
                clearInterval(checkPopupClosed);
                window.removeEventListener('message', handleMessage);
                reject(new Error('로그인이 취소되었습니다.'));
            }
        }, 1000);
    });
};

// 카카오 로그아웃
export const kakaoLogout = async (): Promise<void> => {
    // 로컬에서 로그아웃 처리만 수행
    console.log('Kakao logout');
};

// 초기화 함수 (더 이상 SDK 로드 불필요)
export const initKakao = async () => {
    console.log('Kakao REST API ready');
};
