<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="mgb20">
          <div class="user-info">
            <img src="../assets/img/admin.png" class="user-avator" alt/>
            <div class="user-info-cont">
              <div class="user-info-name">{{ name }}</div>
              <div>{{ role }}</div>
            </div>
          </div>
          <div class="user-info-list">
            性别：<span v-if="user.gender!=null">{{ user.gender }}</span><span v-else>不详</span>
          </div>
          <div class="user-info-list">
            年龄：<span v-if="user.age!=null">{{ user.age }}</span><span v-else>不详</span>
          </div>
          <div class="user-info-list">
            管理员联系Email：<span v-if="user.email!=null">{{ user.email }}</span><span v-else>不详</span>
          </div>
          <!--                    <div class="user-info-list">-->
          <!--                      管理员联系电话：<span>123456789</span>-->
          <!--                    </div>-->
          <div class="user-info-list">
            登录时间：<span>{{ currentTime }}</span>
          </div>
        </el-card>
        <el-card shadow="hover" >
          <template #header>
            <div class="clearfix">
              <span>书籍分类详情</span>
            </div>
          </template>
          <div v-for="(type,index) in page.TypeBookRatio" :key="index">
            {{type.name}}
            <el-progress :percentage="type.ratio" :color="`rgb(${Math.floor(Math.random() * 256)}, ${Math.floor(Math.random() * 256)}, ${Math.floor(Math.random() * 256)})`"></el-progress>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-row :gutter="20" class="mgb20">
          <el-col :span="8">
            <el-card shadow="hover" :body-style="{ padding: '0px' }">
              <div class="grid-content grid-con-1">
                <i class="el-icon-user-solid grid-con-icon"></i>
                <div class="grid-cont-right">
                  <div class="grid-num">{{page.usernum}}</div>
                  <div>用户数量</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" :body-style="{ padding: '0px' }">
              <div class="grid-content grid-con-2">
                <i class="el-icon-message-solid grid-con-icon"></i>
                <div class="grid-cont-right">
                  <div class="grid-num">{{ page.messagenum }}</div>
                  <div>待处理消息数量</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" :body-style="{ padding: '0px' }">
              <div class="grid-content grid-con-3">
                <i class="el-icon-reading grid-con-icon"></i>
                <div class="grid-cont-right">
                  <div class="grid-num">{{page.booknum}}</div>
                  <div>书籍数量</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-card shadow="hover" style="height:580px;">
          <template #header>
            <div class="clearfix">
              <span>审批待办事项</span>
            </div>
          </template>

          <el-table :show-header="false" :data="todoList" style="width:100%;">
            <!--                        <el-table-column width="40">-->
            <!--                            <template #default="scope">-->
            <!--                                <el-checkbox v-model="scope.row.status"></el-checkbox>-->
            <!--                            </template>-->
            <!--                        </el-table-column>-->
            <el-table-column>
              <template #default="scope">
                <div class="todo-item">{{ scope.row.content }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="date" width="180"></el-table-column>
            <el-table-column width="60">
              <template #default="scope">
                <router-link to="/examine">
                  <el-button icon="el-icon-edit" type="text">处理</el-button>
                </router-link>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination

              @size-change="handleSizeChange"
              @current-change="handlePageChange"
              :current-page="query.pageIndex"
              :page-sizes="[5,10,15]"
              :page-size="query.pageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pageTotal"
          >
          </el-pagination>
        </el-card>
      </el-col>
    </el-row>
<!--    <el-row :gutter="20">-->
<!--      <el-col :span="12">-->
<!--        <el-card shadow="hover">-->
<!--          <schart ref="bar" class="schart" canvasId="bar" :options="options"></schart>-->
<!--        </el-card>-->
<!--      </el-col>-->
<!--      <el-col :span="12">-->
<!--        <el-card shadow="hover">-->
<!--          <schart ref="line" class="schart" canvasId="line" :options="options2"></schart>-->
<!--        </el-card>-->
<!--      </el-col>-->
<!--    </el-row>-->
    <el-row :gutter="20">
      <!--        <el-col :xs="24" :sm="24" :lg="8">-->
      <!--          <div class="chart-wrapper">-->
      <!--            <raddar-chart />-->
      <!--          </div>-->
      <!--        </el-col>-->
      <el-col :xs="24" :sm="24" :lg="12">
        <div class="chart-wrapper">
          <raddar-chart/>
        </div>
      </el-col>
      <el-col :xs="24" :sm="24" :lg="12">
        <div class="chart-wrapper">
          <line-chart/>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <!--        <el-col :xs="24" :sm="24" :lg="8">-->
      <!--          <div class="chart-wrapper">-->
      <!--            <raddar-chart />-->
      <!--          </div>-->
      <!--        </el-col>-->
      <el-col :xs="24" :sm="24" :lg="12">
        <div class="chart-wrapper">
          <pie-chart/>
        </div>
      </el-col>
      <el-col :xs="24" :sm="24" :lg="12">
        <div class="chart-wrapper">
          <bar-chart/>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import Schart from "vue-schart";
import {reactive, ref} from "vue";
import RaddarChart from "./chart/RaddarChart.vue";
import PieChart from "./chart/PieChart.vue";
import BarChart from "./chart/BarChart.vue";
import {useStore} from "vuex";
import store from "../store";
import request from "../utils/request";
import router from "../router";
import LineChart from "./chart/LineChart.vue";

export default {
  name: "dashboard",
  components: {
    Schart,
    RaddarChart,
    PieChart,
    BarChart,
    LineChart
  },
  setup() {
    const store = useStore();
    const name = localStorage.getItem("ms_username");
    const currentTime = localStorage.getItem("currentTime");
    const userid = localStorage.getItem("ms_userid")
    const user = reactive({
      age: null,
      gender: null,
      phone: null,
      email: null,
    });
    const role = "管理员";

    const data = reactive([
      {
        name: "2018/09/04",
        value: 1083,
      },
      {
        name: "2018/09/05",
        value: 941,
      },
      {
        name: "2018/09/06",
        value: 1139,
      },
      {
        name: "2018/09/07",
        value: 816,
      },
      {
        name: "2018/09/08",
        value: 327,
      },
      {
        name: "2018/09/09",
        value: 228,
      },
      {
        name: "2018/09/10",
        value: 1065,
      },
    ]);
    const options = {
      type: "bar",
      title: {
        text: "最近一周图书借阅情况",
      },
      xRorate: 25,
      labels: ["周一", "周二", "周三", "周四", "周五"],
      datasets: [
        {
          label: "家电",
          data: [234, 278, 270, 190, 230],
        },
        {
          label: "百货",
          data: [164, 178, 190, 135, 160],
        },
        {
          label: "食品",
          data: [144, 198, 150, 235, 120],
        },
      ],
    };
    const options2 = {
      type: "line",
      title: {
        text: "最近几个月各类书籍借阅趋势图",
      },
      labels: ["6月", "7月", "8月", "9月", "10月"],
      datasets: [
        {
          label: "家电",
          data: [234, 278, 270, 190, 230],
        },
        {
          label: "百货",
          data: [164, 178, 150, 135, 160],
        },
        {
          label: "食品",
          data: [74, 118, 200, 235, 90],
        },
      ],
    };
    const todoList = ref([]);
    const pageTotal = ref(0);
    const query = reactive({
      pageIndex: 1,//当前页号
      pageSize: 5,//每页数量
    });
    const page = reactive({
      usernum:null,
      messagenum:null,
      booknum:null,
      TypeBookRatio:null,
    });
    const TypeBookCopyCount = ref([]);
    // 获取表格数据
    const getData = () => {
      console.log()
      request.get(`/admin/waitingApproves/${query.pageIndex}/${query.pageSize}`).then(res => {
        if (res.code === 455) {
          todoList.value = res.data.list.list;
          pageTotal.value = res.data.WaitingCount;
        }
      })
      request.get("/admin/getCount").then(res => {
        if (res.code === 455) {
          localStorage.setItem("messageNum", res.data);
        }
      })
      request.get(`/admin/info/${userid}`).then(res => {
        if (res.code === 455) {
          user.age = res.data.userInfo.age
          user.gender = res.data.userInfo.gender
          user.email = res.data.userInfo.email
          user.phone = res.data.userInfo.phone
        }
      })
      request.get("/admin/statistics/Count").then(res => {
        if (res.code === 455) {
            page.booknum=res.data.BookNum
            page.messagenum=res.data.WaitingApprovesNum
            page.usernum=res.data.ReaderNum
            page.TypeBookRatio=res.data.TypeBookRatio
        }
      })

    };
    getData();
    // 分页导航
    const handlePageChange = (val) => {
      query.pageIndex = val;
      getData();
    };
    //改变当前每页个数触发
    const handleSizeChange = (val) => {
      query.pageSize = val;
      getData();
    };
    return {
      query,
      pageTotal,
      name,
      data,
      user,
      options,
      options2,
      todoList,
      role,
      currentTime,
      page,
      handlePageChange,
      handleSizeChange,
    };
  },
  computed: {},
  created() {
  }
  ,
  data() {
    return {}
  }
  ,
  methods: {}
};
</script>

<style scoped>
.el-row {
  margin-bottom: 20px;
}

.grid-content {
  display: flex;
  align-items: center;
  height: 100px;
}

.grid-cont-right {
  flex: 1;
  text-align: center;
  font-size: 14px;
  color: #999;
}

.grid-num {
  font-size: 30px;
  font-weight: bold;
}

.grid-con-icon {
  font-size: 50px;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
  color: #fff;
}

.grid-con-1 .grid-con-icon {
  background: rgb(45, 140, 240);
}

.grid-con-1 .grid-num {
  color: rgb(45, 140, 240);
}

.grid-con-2 .grid-con-icon {
  background: rgb(100, 213, 114);
}

.grid-con-2 .grid-num {
  color: rgb(45, 140, 240);
}

.grid-con-3 .grid-con-icon {
  background: rgb(242, 94, 67);
}

.grid-con-3 .grid-num {
  color: rgb(242, 94, 67);
}

.user-info {
  display: flex;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 2px solid #ccc;
  margin-bottom: 20px;
}

.user-avator {
  width: 120px;
  height: 120px;
  border-radius: 50%;
}

.user-info-cont {
  padding-left: 50px;
  flex: 1;
  font-size: 14px;
  color: #999;
}

.user-info-cont div:first-child {
  font-size: 30px;
  color: #222;
}

.user-info-list {
  font-size: 14px;
  color: #999;
  line-height: 25px;
}

.user-info-list span {
  margin-left: 20px;
}

.mgb20 {
  margin-bottom: 20px;
}

.todo-item {
  font-size: 14px;
}

.todo-item-del {
  text-decoration: line-through;
  color: #999;
}

.schart {
  width: 100%;
  height: 300px;
}

.chart-wrapper {
  background: #fff;
  padding: 16px 16px 0;
  margin-bottom: 20px;
}

/*修改组件的默认需要加/deep/*/
:deep(.el-pagination) {
  padding: 20px 5px;
}
</style>
