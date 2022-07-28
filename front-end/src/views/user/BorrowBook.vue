<template>
      <div class="handle-box">
        <el-button type="danger" icon="el-icon-delete" plain @click="handleDelete2">批量删除</el-button>
        <el-input v-model="query.name" placeholder="如输入书名" class="handle-input mr10"></el-input>
        <el-button type="primary" icon="el-icon-search"  @click="handleSearch">搜索</el-button>
      </div>
      <el-table :data="tableData" border class="table" ref="multipleTable" header-cell-class-name="table-header"
                @selection-change="handleSelectionChange" style="background-color: #eef1ee;width: 60%;margin: auto;text-align: center;" :header-cell-style="{background:'#000'}"
      >
        <el-table-column type="selection" width="55" align="center" :selectable="selectable"/>
        <el-table-column prop="title" label="书名" fixed></el-table-column>
        <el-table-column prop="publisher" label="出版社"></el-table-column>
        <el-table-column prop="borrowTime" label="借阅时间" sortable width="160"></el-table-column>
        <el-table-column prop="planTime" label="截止时间" sortable width="160"></el-table-column>
        <el-table-column prop="status" label="归还状态">
          <template #default="scope">
            <el-button v-if="scope.row.status===1" type="warning" size="small" plain style="font-weight: bold">未归还</el-button>
            <el-button v-else-if="scope.row.status===2" type="success" size="small" plain style="font-weight: bold">已归还</el-button>
            <el-button v-else type="danger" size="small" plain style="font-weight: bold">逾期归还</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status===2" type="text" icon="el-icon-delete" class="red"
                       @click="handleDelete(scope.$index, scope.row)">删除
            </el-button>
            <el-button v-else type="text" icon="el-icon-notebook-1" @click="handleReturn(scope.$index, scope.row)">归还
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination" style="text-align: center">
        <el-pagination background layout="total,sizes, prev, pager, next,jumper" :current-page="query.pageIndex"
                       :page-size="query.pageSize" :total="pageTotal" :page-sizes="[5, 10, 20]"
                       @current-change="handlePageChange" @size-change="handleSizeChange"></el-pagination>
      </div>
</template>

<script>
import { ref, reactive} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import request from "../../utils/request";

export default {
  name: "borrowbook",
  setup() {
    const query = reactive({
      name: null,//搜索
      pageIndex: 1,//当前页号
      pageSize: 5,//每页数量
    });
    const tableData = ref([]);
    const pageTotal = ref(0);
    const userid = localStorage.getItem("ms_userid")
    // 获取表格数据
    const getData = () => {
      if (query.name==='')
        query.name=null
      request.get(`/lib/myRecord/${userid}/${query.pageIndex}/${query.pageSize}/${query.name}`).then(res => {
        if (res.code===455 ) {
          tableData.value = res.data.list;
          pageTotal.value = res.data.total;
        }
      })
    };
    getData();

    // 查询操作
    const handleSearch = () => {
      query.pageIndex = 1;
      getData();
    };
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
    // 删除操作
    const handleDelete = (index,row) => {
      // 二次确认删除
      ElMessageBox.confirm("确定要删除吗？", "提示", {
        type: "warning",
      })
          .then(() => {
            request.post("/lib/delete",row).then(res => {
              if (res.code===334 ) {
                ElMessage.success("删除成功");
                getData()
                // tableData.value.splice(index, 1);
              }else{
                ElMessage.error(res.msg)
              }
            })
          })
          .catch(() => {
          });
    };
    // 归还操作
    const handleReturn = (index, row) => {
      // 二次确认删除
      ElMessageBox.confirm("确定要归还吗？", "提示", {
        type: "warning",
      })
          .then(() => {
            request.put("/lib/return",{id:row.id,readerId:parseInt(userid)}).then(res => {
              if (res.code===457 ) {
                ElMessage.success("提交成功");
                getData()
                // tableData.value.splice(index, 1);
              }else{
                ElMessage.error(res.msg)
              }
            })
          })
          .catch(() => {
          });
    };
    const selectable=(row, index)=>
    {
      // status是2才能被选中
      if(row.status ===2){
        return true;
      }
      // 函数必须有返回值且是布尔值
      // 页面刷新后该函数会执行 N 次进行判断(N 为表格行数)
      // 如果没有返回值则默认返回false(全部无法选中)
    };
    return {
      query,
      tableData,
      pageTotal,
      handleSearch,
      handlePageChange,
      handleSizeChange,
      handleDelete,
      handleReturn,
      selectable
    };
  },
  data() {
    return {
      multipleSelection: [],//多行选中的数据
      multipleIndex:null,//选中行的index
      multipleList:{},//选中行的id的数组
    }
  },
  methods: {
    handleSelectionChange(val) {
      this.multipleSelection = val;
      this.multipleIndex=val.map(row => this.tableData.indexOf(row));//获取多选中行的index
      let indexList=[];
      for (let i of this.multipleSelection){
        indexList.push(i.id)
      }
      this.multipleList.ids=indexList;
    },
    handleDelete2(){
      if(this.multipleSelection.length===0){
        ElMessage.warning("请选中要删除的信息")
      }else {
        // 二次确认删除
        ElMessageBox.confirm("确定要删除吗？", "提示", {
          type: "warning",
        })
            .then(() => {
              request.post("/lib/deletes",this.multipleList).then(res => {
                if (res.code===334 ) {
                  ElMessage.success("删除成功");
                  for (let i = this.multipleSelection.length - 1; i >= 0; i--) {
                    this.tableData.splice(this.multipleIndex[i], 1)
                  }
                }else{
                  ElMessage.error(res.msg)
                }
              })
              // for (let i = this.multipleSelection.length - 1; i >= 0; i--) {
              //   this.tableData.splice(this.multipleIndex[i], 1)
              // }
              // tableData.value.splice(index, 1);
            })
            .catch(() => {
            })
      }
    },
  }
};
</script>

<style scoped>
.handle-box {
  margin-top: 20px;
  margin-bottom: 20px;
}

.handle-select {
  width: 120px;
}

.handle-input {
  margin-left: 10px;
  width: 300px;
  display: inline-block;
}

.table {
  width: 100%;
  font-size: 14px;
}

.red {
  color: #ff0000;
}

.mr10 {
  margin-right: 10px;
}

.table-td-thumb {
  display: block;
  margin: auto;
  width: 40px;
  height: 40px;
}
</style>
