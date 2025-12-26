import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import KakaoCallback from './KakaoCallback';
import reportWebVitals from './reportWebVitals';

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

// 간단한 라우팅
const CurrentPage = () => {
  if (window.location.pathname === '/oauth/kakao/callback') {
    return <KakaoCallback />;
  }
  return <App />;
};

root.render(
  <React.StrictMode>
    <CurrentPage />
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
