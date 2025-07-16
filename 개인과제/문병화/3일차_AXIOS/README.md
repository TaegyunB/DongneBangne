# 3. Axios

---

## Axios란?

---

- 브라우저, Node.js를 위한 Promise API를 활용하는 HTTP 비동기통신 라이브러리
- 운영 환경에 따라 브라우저의 XMLHttpRequest 객체 또는 Node.js의 HTTP API 사용
- 간단하므로 HTTP 요청을 보내고 응답하는 것이 쉽다.
- 서버에서 받은 응답을 자동으로 JSON으로 파싱한다.
- 요청이나 응답을 가로채서 중간에 로직을 추가하거나 헤더를 수정하는 작업을 할 수 있다.
- 다양한 에러 처리 기능을 제공한다.

## 기본 문법

---

```jsx
// GET 요청
axios({
  method: 'get',
  url: url,
  params: {
    id: 1,
    category: 'review'
  },
  headers: {
    Authorization: 'Bearer YourAccessToken'
  }
})
.then((res) => {
  console.log(res.data);
})
.catch((err) => {
  console.error(err);
});

// POST 요청
axios({
  method: 'post',
  url: url,
  data: {
    name: 'Dev',
    title: 'good review'
  }
});
```

```jsx
axios(config)
```

- 기본적으로 위와 같은 형태를 띄며, 요청에 대한 정보를 `config` 객체에 담아 전송한다.
    
    `config` 옵션은 다음과 같다.
    
    | config | 설명 |
    | --- | --- |
    | method | 요청을 보낼 URL 주소 지정 |
    | url | HTTP 요청 메서드.  |
    | params | URL의 쿼리 매개변수 |
    | headers | 요청 헤더. 사용자 정의 헤더 추가 가능 |
    | data | 요청 body에 보낼 데이터 설정 |
    | baseURL | 공통적으로 사용하는 기본 URL |
    | responseType | 서버에서 받을 응답 데이터 형식 |
    | withCredentialss | 쿠키 및 HTTP 인증 정보를 포함하도록 설정하는 옵션 |
    | auth | 요청에 대한 HTTP 기본 인증. 사용자 이름과 암호 제공 |

- 이를 통해 실제 사용 예시는 다음과 같다.
    
    ```jsx
    // GET 요청
    axios({
      method: 'get',
      url: url,
      params: {
        id: 1,
        category: 'review'
      },
      headers: {
        Authorization: 'Bearer YourAccessToken'
      }
    })
    .then((res) => {
      console.log(res.data);
    })
    .catch((err) => {
      console.error(err);
    });
    
    // POST 요청
    axios({
      method: 'post',
      url: url,
      data: {
        name: 'Dev',
        title: 'good review'
      }
    });
    ```
    

## 단축 옵션 요청 메서드

---

- 편의에 따라 사용할 수 있는 다양한 요청 메서드가 존재한다. 가장 많이 사용되는 형태는, GET, POST, PUT, DELETE 요청에 대한 단축 메서드이다.
- 다음 예제코드는, 어플리케이션에서 서버로 데이터를 요청할 때 사용하는 HTTP 메서드인 GET 요청을 보낼 때 사용하는 메서드이다.

```jsx
axios.get('https://raminocoding.com/review')
  .then((res) => {
    // 성공한 경우 응답 처리
    console.log('응답 완료 : ', res.data);
  })
  .catch((err) => {
    console.error('에러 : ', err);
});
```

- API의 엔드포인트로 `url` 을 설정하고 이를 이용하여 `axios.get(url)` 을 사용하여 GET 요청을 보낸다.
    
    이후 `then()` 과 `catch()` 를 통해 성공 및 실패 처리를 정의할 수 있다.
    

```jsx
methods: {
	GetData() {
		axios.get(url)
		  .then((res) => {
		    // 성공한 경우 응답 처리
		    console.log('응답 완료 : ', res.data);
		  })
		  .catch((err) => {
		    console.error('에러 : ', err);
		});
	}
}
```

- `vue.js` 에서 동작하기 위해서는 위와 같이 작성해야 한다