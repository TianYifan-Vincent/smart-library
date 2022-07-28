<template>
  <el-menu
      class="el-menu-demo"
      mode="horizontal"
      background-color="#545c64"
      text-color="#fff"
      active-text-color="#ffd04b"
      :router="true"
  >
    <el-menu-item index="1" route="/bookslist">首页</el-menu-item>

    <el-menu-item index="2" route="/bookgrid">查阅</el-menu-item>
    <!--    <el-menu-item index="3" class="right" route="/login" v-if="!islogin">登录</el-menu-item>-->
    <el-submenu index="3" style="float: right">
      <template #title>{{username}}</template>
      <el-menu-item index="3-1" route="/reader">个人信息</el-menu-item>
      <el-menu-item index="3-2" route="/borrowbook">我的借阅</el-menu-item>
      <!--      <el-menu-item index="2-3" route="/updatePass">修改密码</el-menu-item>-->
      <el-menu-item index="3-3" @click="logout">注销</el-menu-item>
    </el-submenu>
  </el-menu>
</template>
<script>
import store from "../../store";

export default {
  name: "Navbar",
  setup(){
    const username = localStorage.getItem("ms_username");
    return{
      username
    }
  },
  data() {
    return {
      user: {},
    };
  },
  computed: {
  },
  methods: {
    logout: function () {
      localStorage.removeItem("currentTime");
      localStorage.removeItem("ms_username");
      localStorage.removeItem("ms_userid");
      localStorage.removeItem("role");
      // localStorage.removeItem("token");
      store.commit('delToken')
      this.$router.push({
        //你需要接受路由的参数再跳转
        path: "/login"
      });
    },
  },
  created() {
  }
};
</script>
<style scoped>

.el-menu-item {
  text-align: center;
}
</style>
<style>
ul.el-menu--popup-bottom-start {
  min-width: 100px;
}
</style>

