<template>
    <div class="">
        <div class="crumbs">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item><i class="el-icon-lx-copy"></i> 借阅模块</el-breadcrumb-item>
              <el-breadcrumb-item>信息管理</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
      <div class="container">
        <div class="handle-box">
          <el-button type="danger" icon="el-icon-delete" plain @click="handleDelete2">批量删除</el-button>
          <el-input v-model="query.name" placeholder="如输入书名" class="handle-input mr10"></el-input>
          <el-button type="primary" icon="el-icon-search"  @click="handleSearch">搜索</el-button>
        </div>
        <el-table :data="tableData" border class="table" ref="multipleTable" header-cell-class-name="table-header"
                  @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" :selectable="selectable"/>
          <el-table-column prop="id" label="借阅ID" sortable fixed></el-table-column>
          <el-table-column prop="title" label="书名"></el-table-column>
          <el-table-column prop="loginName" label="用户名"></el-table-column>
          <el-table-column prop="publisher" label="出版社"></el-table-column>
          <el-table-column prop="borrowTime" label="借阅时间" sortable></el-table-column>
          <el-table-column prop="returnTime" label="归还时间" sortable></el-table-column>
          <el-table-column prop="status" label="归还状态">
            <template #default="scope">
              <span v-if="scope.row.status===1" style="color: #f56c6c">未归还</span>
              <span v-else-if="scope.row.status===2" style="color: #00a854">已归还</span>
              <span v-else style="color:#ffc300;">逾期未还</span>
            </template>
            </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="scope">
              <el-button v-if="scope.row.status===2" type="text" icon="el-icon-delete" class="red"
                         @click="handleDelete(scope.$index, scope.row)">删除
              </el-button>
              <el-button v-else type="text">不可删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination background layout="total,sizes, prev, pager, next,jumper" :current-page="query.pageIndex"
                         :page-size="query.pageSize" :total="pageTotal" :page-sizes="[5, 10, 20]"
                         @current-change="handlePageChange" @size-change="handleSizeChange"></el-pagination>
        </div>
      </div>
    </div>
</template>

<script>
import {inject, ref, reactive} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {fetchData} from "../api/index";
import request from "../utils/request";

export default {
  name: "tabs",
  setup() {
    const query = reactive({
      name: null,//搜索
      pageIndex: 1,//当前页号
      pageSize: 10,//每页数量
    });
    const tableData = ref([]);
    const pageTotal = ref(0);
    // 获取表格数据
    const getData = () => {
      if (query.name==='')
        query.name=null
      request.get(`/admin/borrowReturn/${query.pageIndex}/${query.pageSize}/${query.name}`).then(res => {
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
    const handleDelete = (index) => {
      // 二次确认删除
      ElMessageBox.confirm("确定要删除吗？", "提示", {
        type: "warning",
      })
          .then(() => {
            request.delete(`/admin/borrowReturn/delete/${tableData.value[index].id}`).then(res => {
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
    // 表格编辑时弹窗和保存
    const editVisible = ref(false);
    let form = reactive({
      id: null,
      title: "",
      loginName: "",
      publisher: "",
      borrowTime: "",
      returnTime: "",
      status:""
    });
    let idx = -1;
    const handleEdit = (index, row) => {
      idx = index;
      Object.keys(form).forEach((item) => {
        form[item] = row[item];
      });
      editVisible.value = true;
    };
    const renovate = inject("reload");
    const saveEdit = () => {
      editVisible.value = false;
      ElMessage.success(`修改第 ${idx + 1} 行成功`);
      Object.keys(form).forEach((item) => {
        tableData.value[idx][item] = form[item];
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
      editVisible,
      form,
      renovate,
      handleSearch,
      handlePageChange,
      handleSizeChange,
      handleDelete,
      handleEdit,
      saveEdit,
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
              console.log(this.multipleList)
              request.post("/admin/borrowReturn/delete",this.multipleList).then(res => {
                if (res.code===334 ) {
                  ElMessage.success("删除成功");
                  for (let i = this.multipleSelection.length - 1; i >= 0; i--) {
                    this.tableData.splice(this.multipleIndex[i], 1)
                  }
                }else{
                  ElMessage.error(res.msg)
                }
              })
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
