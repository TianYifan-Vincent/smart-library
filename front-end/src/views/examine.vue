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
        <el-tab-pane :label="`未读消息(${pageTotal})`" name="first">
          <el-table :data="state.unread" :show-header="false" style="width: 100%">
            <el-table-column>
              <template #default="scope">
                <span class="message-title">{{scope.row.content}}</span>
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
            <el-button type="primary" @click="handleAllRead">全部通过</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="`已读消息(${pageTotal2})`" name="second">
          <template v-if="message === 'second'">
            <el-table :data="state.read" :show-header="false" style="width: 100%">
              <el-table-column>
                <template #default="scope">
                  <span class="message-title">{{scope.row.content}}</span>
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
              <el-button type="danger" @click="handleAllDel">删除全部</el-button>
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
import request from "../utils/request";
export default {
  name: "examine",
  setup() {
    const message = ref("first");
    const state = reactive({
      unread: [
        // {
        //   date: "2018-04-19 20:00:00",
        //   title: "user1借阅《三体》审批中",
        // },
        // {
        //   date: "2018-04-19 21:00:00",
        //   title: "user2归还《三体2》审批中",
        // },
      ],
      read: [
        // {
        //   date: "2018-04-19 20:00:00",
        //   title: "user3借阅《三体》审批中",
        // },
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
    // 获取表格数据
    const getData = () => {
      request.get(`/admin/waitingApproves/${query.pageIndex}/${query.pageSize}`).then(res => {
        if (res.code===455 ) {
          state.unread = res.data.list.list;
          pageTotal.value = res.data.WaitingCount;
          pageTotal2.value=res.data.FinishedCount;
        }
      })
      request.get(`/admin/finishedApproves/${query2.pageIndex}/${query2.pageSize}`).then(res => {
        if (res.code===455 ) {
          state.read = res.data.list.list;
          pageTotal.value = res.data.WaitingCount;
          pageTotal2.value=res.data.FinishedCount;
        }
      })
      request.get("/admin/getCount").then(res => {
        if (res.code === 455) {
          localStorage.setItem("messageNum", res.data);
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
    const userid = localStorage.getItem("ms_userid");//管理员id
    const handleRead = (index) => {
      // const item = state.unread.splice(index, 1);
      // state.read = item.concat(state.read);
      const item=state.unread[index]
      item.userId=parseInt(userid)
      console.log(item);
      request.put("/admin/waitingApproves/1", item).then(res => {
        if (res.code === 457) {
          ElMessage.success(`通过成功`);
          getData()
        } else {
          ElMessage.error(res.msg);
        }
      })
    };
    const handleAllRead = () => {
      // const item = state.unread.splice(index, 1);
      // state.read = item.concat(state.read);
      // item[0].userId=parseInt(userid)
      const item = reactive({
        userId:parseInt(userid)
      });
      request.put("/admin/waitingApproves/all/1", item).then(res => {
        if (res.code === 457) {
          ElMessage.success(`全部通过成功`);
          getData()
        } else {
          ElMessage.error(res.msg);
        }
      })
    };
    const handleRefuse = (index) => {
      // 二次确认删除
      ElMessageBox.confirm("逾期未还", "提示", {
        type: "warning",
      })
          .then(() => {
            const item = state.unread[index];
            // state.read = item.concat(state.read);
            item.userId=parseInt(userid)
            console.log(item)
            request.put("/admin/waitingApproves/0", item).then(res => {
              if (res.code === 457) {
                ElMessage.success(`拒绝成功`);
                getData()
              } else {
                ElMessage.error(res.msg);
              }
            })
            // tableData.value.splice(index, 1);
          })
          .catch(() => {
          })
    };
    const handleDel = (index) => {
      const item = state.read[index];
      console.log(item)
      request.post("/admin/finishedApproves/delete",item).then(res => {
        if (res.code === 334) {
          ElMessage.success("删除成功");
          getData()
        } else {
          ElMessage.error(res.msg);
        }
      })
    };
    const handleAllDel = () => {
      request.post("/admin/finishedApproves/deleteAll",{ID:0}).then(res => {
        if (res.code === 334) {
          ElMessage.success("全部删除成功");
          getData()
        } else {
          ElMessage.error(res.msg);
        }
      })
    };
    // const handleRestore = (index) => {
    //   const item = state.recycle.splice(index, 1);
    //   state.read = item.concat(state.read);
    // };

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
      handleAllRead,
      handleRefuse,
      handleDel,
      handleAllDel
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

