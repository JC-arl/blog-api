import { useEffect } from 'react';

export default function KakaoCallback() {
    useEffect(() => {
        // URL에서 인가 코드 추출
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get('code');
        const error = urlParams.get('error');

        if (window.opener) {
            if (code) {
                // 부모 창으로 인가 코드 전달
                window.opener.postMessage(
                    {
                        type: 'KAKAO_AUTH_CODE',
                        code: code,
                    },
                    window.location.origin
                );
            } else if (error) {
                // 에러 전달
                window.opener.postMessage(
                    {
                        type: 'KAKAO_AUTH_ERROR',
                        error: error,
                    },
                    window.location.origin
                );
            }

            // 팝업 닫기
            window.close();
        }
    }, []);

    return (
        <div style={{ textAlign: 'center', marginTop: '50px' }}>
            <p>로그인 처리 중...</p>
        </div>
    );
}
