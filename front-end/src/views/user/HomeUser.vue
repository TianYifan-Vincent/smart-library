<template>
  <el-container class="app">
    <el-header>
      <Navbar/>
    </el-header>
    <el-main style="background-color: #c8cec1">
      <router-view/>
    </el-main>
  </el-container>
</template>

<script>
import Navbar from "../user/Navbar.vue";
export default {
  name: "App",
  components: {
    Navbar: Navbar,
  }
};
</script>

<style>
.app {
  font-family: "Avenir", Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  height: 100%;
  overflow-y: scroll;
}
body {
  margin: 0;
  padding: 0;
  height: 100vh;
}
.el-header {
  padding: 0 !important;
}
</style>
