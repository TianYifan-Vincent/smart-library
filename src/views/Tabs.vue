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
          <el-table-column type="selection" width="55" align="center"/>
          <el-table-column prop="id" label="借阅ID" sortable fixed></el-table-column>
          <el-table-column prop="title" label="书名"></el-table-column>
          <el-table-column prop="loginName" label="用户名"></el-table-column>
          <el-table-column prop="publisher" label="出版社"></el-table-column>
          <el-table-column prop="borrow_time" label="借阅时间" sortable></el-table-column>
          <el-table-column prop="return_time" label="归还时间" sortable></el-table-column>
          <el-table-column prop="status" label="归还状态"></el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="scope">
              <el-button type="text" icon="el-icon-delete" class="red"
                         @click="handleDelete(scope.$index, scope.row)">删除
              </el-button>
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

export default {
  name: "tabs",
  setup() {
    const query = reactive({
      name: "",//搜索
      pageIndex: 1,//当前页号
      pageSize: 10,//每页数量
    });
    const tableData = ref([]);
    const pageTotal = ref(0);
    // 获取表格数据
    const getData = () => {
      tableData.value = [{id: 1, title:"三体1",loginName: "刘毛毛"}, {id: 2, title:"三体2", loginName: "刘毛毛2"},{id: 3,  title:"三体3",loginName: "刘毛毛3"}]
      pageTotal.value = 1
      // fetchData(query).then((res) => {
      //     tableData.value = res.list;
      //     pageTotal.value = res.pageTotal || 50;
      // });
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
            ElMessage.success("删除成功");
            tableData.value.splice(index, 1);
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
      borrow_time: "",
      return_time: "",
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
    };
  },
  data() {
    return {
      multipleSelection: [],//多行选中的数据
      multipleIndex:null,//选中行的index
    }
  },
  methods: {
    handleSelectionChange(val) {
      this.multipleSelection = val;
      this.multipleIndex=val.map(row => this.tableData.indexOf(row));//获取多选中行的index
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
              ElMessage.success("删除成功");
              // console.log(this.multipleSelection)
              for (let i = this.multipleSelection.length - 1; i >= 0; i--) {
                this.tableData.splice(this.multipleIndex[i], 1)
              }
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
