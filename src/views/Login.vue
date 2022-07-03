<template>
    <div class="login-wrap">
        <div class="ms-login">
            <div class="ms-title">智慧图书馆后台管理系统</div>
            <el-form :model="param" :rules="rules" ref="login" label-width="0px" class="ms-content">
                <el-form-item prop="username">
                    <el-input v-model="param.username" placeholder="username">
                        <template #prepend>
                            <el-button icon="el-icon-user"></el-button>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input type="password" placeholder="password" v-model="param.password"
                        @keyup.enter="submitForm()">
                        <template #prepend>
                            <el-button icon="el-icon-lock"></el-button>
                        </template>
                    </el-input>
                </el-form-item>
                <div class="div1">
                  <a class="login-tips" style="cursor: pointer">忘记密码</a>
                  <a class="login-tips" style="margin-left: 50px;cursor: pointer">注册</a>
                </div>
                <div class="login-btn">
                    <el-button type="primary" @click="submitForm()">登录</el-button>
                </div>
            </el-form>
        </div>
    </div>
</template>

<script>
import { ref, reactive } from "vue";
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";

export default {
    setup() {
        const router = useRouter();
        const param = reactive({
            username: "",
            password: "",
        });

        const rules = {
            username: [
                {
                    required: true,
                    message: "请输入用户名",
                    trigger: "blur",
                },
            ],
            password: [
                { required: true, message: "请输入密码", trigger: "blur" },
            ],
        };
        const login = ref(null);
        const submitForm = () => {
            login.value.validate((valid) => {
                if (valid) {
                    ElMessage.success("登录成功");
                    localStorage.setItem("ms_username", param.username);
                    localStorage.setItem("currentTime",getlastlogintime());
                    router.push("/");
                } else {
                    ElMessage.error("登录失败");
                    return false;
                }
            });
        };

        const lastlogintime = reactive({
          date: '',
          time: '',
        });
        const getlastlogintime=()=>{
        // 获取时间接口
        var myDate = new Date()

        let month = (myDate.getMonth() + 1).toString().padStart(2, '0')

        let day = myDate.getDate().toString().padStart(2, '0')

        let hour = myDate.getHours().toString().padStart(2, '0')

        let minutes = myDate.getMinutes().toString().padStart(2, '0')

        let seconed = myDate.getSeconds().toString().padStart(2, '0')

        lastlogintime.date = myDate.getFullYear() + '-' + month + '-' + day

        lastlogintime.time = hour + ':' + minutes + ':' + seconed
        return lastlogintime.date+lastlogintime.time
      }
        const store = useStore();
        store.commit("clearTags");

        return {
            param,
            rules,
            login,
            lastlogintime,
            submitForm,
            getlastlogintime
        };
    },
};
</script>

<style scoped>
.login-wrap {
    position: relative;
    width: 100%;
    height: 100%;
    background-image: url(../assets/img/login-bg.jpg);
    background-size: 100%;
}
.ms-title {
    width: 100%;
    line-height: 50px;
    text-align: center;
    font-size: 20px;
    color: #b06267;
    border-bottom: 1px solid #ddd;
}
.ms-login {
    position: absolute;
    left: 50%;
    top: 50%;
    width: 350px;
    margin: -190px 0 0 -175px;
    border-radius: 5px;
    background: rgba(255, 255, 255, 0.3);
    overflow: hidden;
}
.ms-content {
    padding: 30px 30px;
}
.login-btn {
    text-align: center;
}
.login-btn button {
    width: 100%;
    height: 36px;
    margin-bottom: 10px;
}
.login-tips {
    font-size: 12px;
    line-height: 30px;
    color: #62afff;
}
.div1{
  display: flex;
  justify-content:center;
  align-items:center;
}
</style>
