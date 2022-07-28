import axios from 'axios'
import store from "../store";
import router from "../router";

const request = axios.create({
    timeout: 12000   //请求超时时间
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
// 比如统一加token，对请求参数统一加密
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    config.headers['token'] = store.state.token
    // config.headers['token'] = token;  // 设置请求头
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        return res;
    },
    error => {
        // console.log(error)
        // switch (error.response.status) {
        //     case 401:
        //         this.$store.commit('del_token');
        //         router.push({
        //             path: '/login',
        //             query: {redirect: router.currentRoute.value.fullPath}//登录成功后跳入浏览的当前页面,vue3需要加.value
        //         })
        // }
        console.log('err' + error) // for debug
        return Promise.reject(error)
    }
)


export default request

