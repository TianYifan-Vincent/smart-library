<template>
    <div class="login-wrap">
        <div class="ms-login" v-if="is_login">
            <div class="ms-title">智慧图书管理系统</div>
            <el-form :model="param" :rules="rules" ref="login" label-width="0px" class="ms-content">
                <el-form-item prop="loginName">
                    <el-input v-model="param.loginName" placeholder="用户名">
                        <template #prepend>
                            <el-button icon="el-icon-user"></el-button>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input type="password" show-password placeholder="密码" v-model="param.password"
                        @keyup.enter="submitForm()">
                        <template #prepend>
                            <el-button icon="el-icon-lock"></el-button>
                        </template>
                    </el-input>
                </el-form-item>
                <div class="div1">
                  <a class="login-tips" style="cursor: pointer" @click="loginforget">忘记密码</a>
                  <a class="login-tips" style="margin-left: 50px;cursor: pointer" @click="loginregister">注册</a>
                </div>
                <div class="login-btn">
                    <el-button type="primary" @click="submitForm()">登录</el-button>
                </div>
            </el-form>
        </div>
<!--        忘记密码界面-->
        <div class="ms-login" v-else-if="is_forget">
          <div class="ms-title">更改密码</div>
          <el-form :model="param3" :rules="rules3" ref="forget" label-width="0px" class="ms-content">
            <el-form-item prop="loginName">
              <el-input v-model="param3.loginName" placeholder="用户名">
                <template #prepend>
                  <el-button icon="el-icon-user"></el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="email">
              <el-input v-model="param3.email" placeholder="邮箱">
                <template #prepend>
                  <el-button icon="el-icon-message"></el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="confirmCode" style="margin-bottom: 17px">
              <el-input v-model="param3.confirmCode" placeholder="请输入验证码">
              </el-input>
              <el-button slot="suffix" size="mini" style="border: none;" @click="sendcode" :disabled="!show"
              >获取验证码
                <span v-show="!show">({{count}}s)</span>
              </el-button>
            </el-form-item>
            <el-form-item prop="password">
              <el-input type="password" show-password placeholder="密码" v-model="param3.password">
                <template #prepend>
                  <el-button icon="el-icon-lock"></el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="confirm">
              <el-input type="password" show-password placeholder="重新输入密码" v-model="param3.confirm"
                        @keyup.enter="submitregister()">
                <template #prepend>
                  <el-button icon="el-icon-lock"></el-button>
                </template>
              </el-input>
            </el-form-item>
            <div class="div1">
              <a class="login-tips" style="cursor: pointer" @click="forgetregister">注册</a>
              <a class="login-tips" style="margin-left: 50px;cursor: pointer" @click="forgetlogin">登录</a>
            </div>
            <div class="login-btn">
              <el-button type="primary" @click="changepswd">更改密码</el-button>
            </div>
          </el-form>
        </div>
      <!--        用户注册界面-->
      <div class="ms-login" v-else-if="is_register">
        <div class="ms-title">用户注册</div>
        <el-form :model="param2" :rules="rules2" ref="register" label-width="0px" class="ms-content">
          <el-form-item prop="loginName">
            <el-input v-model="param2.loginName" placeholder="用户名(6-15位数字字母组合)">
              <template #prepend>
                <el-button icon="el-icon-user"></el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="param2.email" placeholder="邮箱">
              <template #prepend>
                <el-button icon="el-icon-message"></el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input type="password" show-password placeholder="密码" v-model="param2.password">
              <template #prepend>
                <el-button icon="el-icon-lock"></el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="confirm">
            <el-input type="password" show-password placeholder="重新输入密码" v-model="param2.confirm"
                      @keyup.enter="submitregister()">
              <template #prepend>
                <el-button icon="el-icon-lock"></el-button>
              </template>
            </el-input>
          </el-form-item>
          <div class="div1">
            <a class="login-tips" style="cursor: pointer" @click="registerforget">忘记密码</a>
            <a class="login-tips" style="margin-left: 50px;cursor: pointer" @click="registerlogin">登录</a>
          </div>
          <div class="login-btn">
            <el-button type="primary" @click="submitregister()">完成注册</el-button>
          </div>
        </el-form>
      </div>
    </div>
</template>

<script>
import {inject, reactive, ref} from "vue";
import {useStore} from "vuex";
import {useRouter} from "vue-router";
import {ElMessage} from "element-plus";
import request from "../utils/request";

export default {
    setup() {
        const router = useRouter();
        const param = reactive({
            loginName: "",
            password: "",
        });
        const param2 = reactive({
          loginName: "",
          password: "",
          confirm: "",
          email:""
        });
        const param3 = reactive({
          loginName: "",
          password: "",
          confirm: "",
          confirmCode:"",
          email:""
        });
        //刷新图标  //刷新事件
        const renovate = inject("reload");
        const rules = {
            loginName: [
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
      const judgemail = (rule, value, callback) => {//邮箱检验规则
        let reg = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
        if (value === '') {
          callback(new Error('请输入邮箱'));
        } else if (!reg.test(value)) {
          callback(new Error('邮箱格式错误'));
        } else {
          callback();
        }
      };
      const register = ref(null);
      const validatePass = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请输入密码'));
        } else {
          if (param2.confirm !== '') {
            if (!register.value) return
            register.value.validateField('confirm');
          }
          callback();
        }
      };
      const validatePass2 = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请再次输入密码'));
        } else if (value !== param2.password) {
          callback(new Error('两次输入密码不一致!'));
        } else {
          callback();
        }
      };
      const rules2 = {
        loginName: [
          {
            required: true,
            message: "请输入用户名",
            trigger: "blur"
          },
          {
            min:6,
            max: 15,
            message: "长度为6-15位字符",
            trigger: "blur",
          },
        ],
        password: [
          { validator: validatePass, trigger: 'blur' },
        ],
        confirm: [
          { validator:validatePass2, trigger: 'blur'},
        ],
        email: [
          {
            validator:judgemail,
            trigger: "blur"
          }
        ],
      };
      const forget = ref(null);
      const validatePass_3 = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请输入密码'));
        } else {
          if (param3.confirm !== '') {
            if (!forget.value) return
            forget.value.validateField('confirm');
          }
          callback();
        }
      };
      const validatePass2_3 = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请再次输入密码'));
        } else if (value !== param3.password) {
          callback(new Error('两次输入密码不一致!'));
        } else {
          callback();
        }
      };
      const rules3 = {
        loginName: [
          {
            required: true,
            message: "请输入用户名",
            trigger: "blur"
          },
          {
            min:6,
            max: 15,
            message: "长度为6-15位字符",
            trigger: "blur",
          },
        ],
        password: [
          { validator: validatePass_3, trigger: 'blur' },
        ],
        confirm: [
          { validator:validatePass2_3, trigger: 'blur'},
        ],
        confirmCode:[{
          required: true,
          message: "请输入验证码",
          trigger: "blur"
        }],
        email: [
          {
            validator:judgemail,
            trigger: "blur"
          }
        ],
      };
        const login = ref(null);
        const submitForm = () => {
            login.value.validate((valid) => {
                if (valid) {
                  request.get(`/login/${param.loginName}/${param.password}`).then(res => {
                    // console.log(res[0].data)
                    if (res.code === 201||res.code===202) {
                      ElMessage.success("登录成功");
                      localStorage.setItem("ms_username", param.loginName);
                      localStorage.setItem("token", res.data.token);
                      localStorage.setItem("ms_userid", res.data.user.id);
                      store.commit('getuserInfo',res.data.user);
                      store.commit('setToken',res.data.token);
                      localStorage.setItem("currentTime",getlastlogintime());
                      if (res.msg === "管理员成功"){
                        localStorage.setItem("role", 'admin');//1表示管理员
                        router.push("/")  //登陆成功后进行页面跳转，跳转到客户页面
                      }
                      else{
                        localStorage.setItem("role", 'user');//0表示读者
                        router.push("/readerpage")  //登陆成功后进行页面跳转，跳转到维修员页面
                      }
                    } else {
                      ElMessage.error(res.msg);
                    }
                  })
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

        let second = myDate.getSeconds().toString().padStart(2, '0')

        lastlogintime.date = myDate.getFullYear() + '-' + month + '-' + day

        lastlogintime.time = hour + ':' + minutes + ':' + second
          return lastlogintime.date + ' ' + lastlogintime.time
      }
        const store = useStore();
        store.commit("clearTags");

        return {
            param,
            param2,
            param3,
            rules,
            rules2,
            rules3,
            login,
            register,
            forget,
            lastlogintime,
            renovate,
            submitForm,
            getlastlogintime,
        };
    },
  data(){
      return{
        is_login:true,
        is_register:false,
        is_forget:false,
        timer:null,//计时器
        count:"",//倒计时
        show:true,//控制按钮
      }
  },
  methods:{
    loginregister(){
      this.$refs.login.resetFields()
      this.is_login=false
      this.is_register=true
      renovate()
    },
    loginforget(){
      this.$refs.login.resetFields()
      this.is_login=false
      this.is_forget=true
      renovate()
    },
    registerlogin(){
      this.$refs.register.resetFields()
      this.is_register=false
      this.is_login=true
      renovate()
    },
    registerforget(){
      this.$refs.register.resetFields()
      this.is_register=false
      this.is_forget=true
      renovate()
    },
    forgetlogin(){
      this.$refs.forget.resetFields()
      this.is_forget=false
      this.is_login=true
      renovate()
    },
    forgetregister(){
      this.$refs.forget.resetFields()
      this.is_forget=false
      this.is_register=true
      renovate()
    },
    submitregister(){
      this.$refs['register'].validate((valid) => {
        if (valid) {
          request.post("/registry",this.param2).then(res => {
            if (res.code === 454) {
              this.$message({
                type: "success",
                message: "注册成功"
              })
              // this.store.commit('submitClientInfo', res.data)//同步提交个人信息数据
              // this.$router.push("/client")  //登陆成功后进行页面跳转，跳转到主页
            } else {
              this.$message({
                type: "error",
                message: res.msg
              })
            }
          })
        }
      })
    },
    //发送验证码
    sendcode() {
      if(!this.param3.email){
        this.$refs.forget.validateField('email');
      }else{
        const TIME_COUNT = 30;  //倒计时时间
        if (!this.timer) {
          this.count = TIME_COUNT
          this.show = false
          this.timer = setInterval(() => {
            if (this.count > 0 && this.count <= TIME_COUNT) {
              this.count--
            } else {
              this.show = true
              clearInterval(this.timer) // 清除定时器
              this.timer = null
            }
          }, 1000)
        }
        //这里写请求
        request.post("/sendVerCode",this.param3).then(res => {
          if (res.code === 16) {
            this.$message({
              type: "success",
              message: res.msg
            })
          } else {
            this.$message({
              type: "error",
              message: res.msg
            })
          }
        })
      }
    },
    changepswd(){
      request.put("/changeCode",this.param3).then(res => {
        if (res.code === 457) {
          this.$message({
            type: "success",
            message: "修改成功"
          })
        } else {
          this.$message({
            type: "error",
            message: res.msg
          })
        }
      })
    },
  }
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
    color: black;
}
.div1{
  display: flex;
  justify-content:center;
  align-items:center;
}
</style>
