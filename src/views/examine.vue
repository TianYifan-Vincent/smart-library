<template>
  <div class="">
    <div class="crumbs">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item><i class="el-icon-lx-copy"></i> 借阅模块</el-breadcrumb-item>
        <el-breadcrumb-item>审批管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="container">
      <el-tabs v-model="message">
        <el-tab-pane :label="`未读消息(${state.unread.length})`" name="first">
          <el-table :data="state.unread" :show-header="false" style="width: 100%">
            <el-table-column>
              <template #default="scope">
                <span class="message-title">{{scope.row.title}}</span>
              </template>
            </el-table-column>
            <el-table-column prop="date" width="180"></el-table-column>
            <el-table-column width="150">
              <template #default="scope">
                <div style="display: flex">
                <el-button type="success" size="small" @click="handleRead(scope.$index)">通过</el-button>
                <el-button type="danger" style="margin-left: 10px" size="small" @click="handleRefuse(scope.$index)">拒绝</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
              style="margin-top: 20px"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
              :current-page="query.pageIndex"
              :page-sizes="[5,10,15]"
              :page-size="query.pageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pageTotal"
          >
          </el-pagination>
          <div class="handle-row">
            <el-button type="primary">全部通过</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="`已读消息(${state.read.length})`" name="second">
          <template v-if="message === 'second'">
            <el-table :data="state.read" :show-header="false" style="width: 100%">
              <el-table-column>
                <template #default="scope">
                  <span class="message-title">{{scope.row.title}}</span>
                </template>
              </el-table-column>
              <el-table-column prop="date" width="150"></el-table-column>
              <el-table-column width="120">
                <template #default="scope">
                  <el-button type="danger" @click="handleDel(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
                style="margin-top: 20px"
                @size-change="handleSizeChange2"
                @current-change="handlePageChange2"
                :current-page="query2.pageIndex"
                :page-sizes="[5,10,15]"
                :page-size="query2.pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="pageTotal2"
            >
            </el-pagination>
            <div class="handle-row">
              <el-button type="danger">删除全部</el-button>
            </div>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
export default {
  name: "examine",
  setup() {
    const message = ref("first");
    const state = reactive({
      unread: [
        {
          date: "2018-04-19 20:00:00",
          title: "user1借阅《三体》审批中",
        },
        {
          date: "2018-04-19 21:00:00",
          title: "user2归还《三体2》审批中",
        },
      ],
      read: [
        {
          date: "2018-04-19 20:00:00",
          title: "user3借阅《三体》审批中",
        },
      ],
    });
    const query = reactive({
      pageIndex: 1,//当前页号
      pageSize: 10,//每页数量
    });
    const pageTotal = ref(0);
    const query2 = reactive({
      pageIndex: 1,//当前页号
      pageSize: 10,//每页数量
    });
    const pageTotal2 = ref(0);
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
    // 分页导航
    const handlePageChange2 = (val) => {
      query2.pageIndex = val;
      getData();
    };
    //改变当前每页个数触发
    const handleSizeChange2 = (val) => {
      query2.pageSize = val;
      getData();
    };
    const handleRead = (index) => {
      const item = state.unread.splice(index, 1);
      console.log(item);
      state.read = item.concat(state.read);
    };
    const handleRefuse = (index) => {
      // 二次确认删除
      ElMessageBox.confirm("逾期未还", "提示", {
        type: "warning",
      })
          .then(() => {
            ElMessage.success("删除成功");
            // console.log(this.multipleSelection)
            for (let i = this.multipleSelection.length - 1; i >= 0; i--) {
              this.tableData.splice(this.multipleIndex[i], 1)
            }
            // tableData.value.splice(index, 1);
          })
          .catch(() => {
          })
    };
    const handleDel = (index) => {
      const item = state.read.splice(index, 1);
      state.recycle = item.concat(state.recycle);
    };
    const handleRestore = (index) => {
      const item = state.recycle.splice(index, 1);
      state.read = item.concat(state.read);
    };

    return {
      query,
      pageTotal,
      query2,
      pageTotal2,
      message,
      state,
      handlePageChange,
      handleSizeChange,
      handlePageChange2,
      handleSizeChange2,
      handleRead,
      handleRefuse,
      handleDel,
      handleRestore,
    };
  },
};
</script>

<style>
.message-title {
  cursor: pointer;
}
.handle-row {
  margin-top: 30px;
}
</style>

