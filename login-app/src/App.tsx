import React, {useState, useEffect} from "react";
import "./login.css";
import { auth, signInWithGoogle, firebaseSignOut } from "./firebaseConfig";
import { onAuthStateChanged } from "firebase/auth";

// 개발 모드에서는 포트 8080, 프로덕션에서는 같은 origin 사용
const API_BASE_URL = process.env.NODE_ENV === 'development'
    ? "http://localhost:8080"
    : "";

export default function LoginUI() {
    const [isSignup, setIsSignup] = useState(false);
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [nickname, setNickname] = useState("");
    const [error, setError] = useState("");
    const [user, setUser] = useState<any>(null);
    const [firebaseIdToken, setFirebaseIdToken] = useState<string | null>(null);

    // Firebase 인증 상태 감지
    useEffect(() => {
        const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
            if (firebaseUser) {
                // Firebase 로그인 성공
                const idToken = await firebaseUser.getIdToken();
                setFirebaseIdToken(idToken);
                setUser({
                    email: firebaseUser.email,
                    name: firebaseUser.displayName,
                    uid: firebaseUser.uid,
                });

                // 백엔드에 ID Token 전송하여 검증 및 사용자 등록
                try {
                    const response = await fetch(`${API_BASE_URL}/auth/firebase-verify`, {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            "Authorization": `Bearer ${idToken}`
                        },
                    });

                    if (response.ok) {
                        console.log("백엔드 인증 성공");
                    }
                } catch (err) {
                    console.error("백엔드 검증 실패:", err);
                }
            } else {
                setUser(null);
                setFirebaseIdToken(null);
            }
        });

        return () => unsubscribe();
    }, []);

    // 회원가입 (이메일/패스워드 방식)
    const handleSignup = async () => {
        try {
            setError("");
            const response = await fetch(`${API_BASE_URL}/auth/signup`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    email,
                    password,
                    nickname,
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || "회원가입 실패");
            }

            const data = await response.json();
            console.log("Signup successful:", data);

            // 토큰 저장
            localStorage.setItem("accessToken", data.accessToken);
            localStorage.setItem("refreshToken", data.refreshToken);

            alert("회원가입 성공!");
            setUser({ email });
            setIsSignup(false);
            setEmail("");
            setPassword("");
            setNickname("");
        } catch (err: any) {
            console.error("Signup error:", err);
            setError(err.message);
        }
    };

    // 로그인 (이메일/패스워드 방식)
    const handleLogin = async () => {
        try {
            setError("");
            const response = await fetch(`${API_BASE_URL}/auth/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    email,
                    password,
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || "로그인 실패");
            }

            const data = await response.json();
            console.log("Login successful:", data);

            // 토큰 저장
            localStorage.setItem("accessToken", data.accessToken);
            localStorage.setItem("refreshToken", data.refreshToken);

            setUser({ email });
            alert("로그인 성공!");
        } catch (err: any) {
            console.error("Login error:", err);
            setError(err.message);
        }
    };

    // 로그아웃
    const handleLogout = async () => {
        try {
            // Firebase 로그아웃
            if (firebaseIdToken) {
                await firebaseSignOut();
            }

            // 로컬 스토리지 정리
            const accessToken = localStorage.getItem("accessToken");
            if (accessToken) {
                await fetch(`${API_BASE_URL}/auth/logout`, {
                    method: "POST",
                    headers: {
                        "Authorization": `Bearer ${accessToken}`,
                    },
                });
            }

            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            setUser(null);
            setFirebaseIdToken(null);
            alert("로그아웃 성공!");
        } catch (err) {
            console.error("Logout error:", err);
        }
    };

    // Google 로그인 (Firebase)
    const handleGoogleLogin = async () => {
        try {
            setError("");
            await signInWithGoogle();
            // onAuthStateChanged가 자동으로 처리
        } catch (err: any) {
            console.error("Google login error:", err);
            setError(err.message);
        }
    };

    // 로그인된 상태
    if (user) {
        return (
            <div className="container">
                <div className="card">
                    <h1 className="title">환영합니다!</h1>
                    <p style={{ textAlign: "center", marginBottom: "20px" }}>
                        {user.email || user.name}
                    </p>
                    {firebaseIdToken && (
                        <p style={{ fontSize: "12px", color: "#666", marginBottom: "20px", wordBreak: "break-all" }}>
                            Firebase ID Token: {firebaseIdToken.substring(0, 50)}...
                        </p>
                    )}
                    <button className="btn primary" onClick={handleLogout}>
                        로그아웃
                    </button>
                </div>
            </div>
        );
    }

    // 로그인/회원가입 폼
    return (
        <div className="container">
            <div className="card">
                <h1 className="title">{isSignup ? "회원가입" : "로그인"}</h1>

                {error && (
                    <div style={{ color: "red", marginBottom: "10px", textAlign: "center" }}>
                        {error}
                    </div>
                )}

                <div className="form-group">
                    <input
                        type="email"
                        placeholder="이메일"
                        className="input"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                    <input
                        type="password"
                        placeholder="비밀번호"
                        className="input"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />

                    {isSignup && (
                        <input
                            type="text"
                            placeholder="닉네임"
                            className="input"
                            value={nickname}
                            onChange={(e) => setNickname(e.target.value)}
                        />
                    )}

                    <button
                        className="btn primary"
                        onClick={isSignup ? handleSignup : handleLogin}
                    >
                        {isSignup ? "회원가입" : "로그인"}
                    </button>

                    <button
                        className="btn secondary"
                        onClick={() => setIsSignup(!isSignup)}
                        style={{ marginTop: "10px" }}
                    >
                        {isSignup ? "로그인으로 전환" : "회원가입으로 전환"}
                    </button>

                    <div style={{ margin: "20px 0", textAlign: "center" }}>
                        - 또는 -
                    </div>

                    <button className="btn secondary" onClick={handleGoogleLogin}>
                        Google로 로그인 (Firebase)
                    </button>
                </div>
            </div>
        </div>
    );
}
