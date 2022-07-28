import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import installElementPlus from './plugins/element'
import './assets/css/icon.css'
import axios from "axios";
import moment from 'moment'

const app = createApp(App)
app.config.globalProperties.$http=axios
axios.defaults.baseURL='http://localhost:9090'

installElementPlus(app)
app
    .use(store)
    .use(router)
    .mount('#app')

