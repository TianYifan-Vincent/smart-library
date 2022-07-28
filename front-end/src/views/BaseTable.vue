<template>
  <div>
    <div class="crumbs">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>
          <i class="el-icon-lx-cascades"></i> 所有用户
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="container">
      <div class="handle-box">
        <el-button type="danger" icon="el-icon-delete" plain @click="handleDelete2">批量删除</el-button>
        <el-input v-model="query.name" placeholder="如输入用户名" class="handle-input mr10"></el-input>
        <el-button type="primary" icon="el-icon-search"  @click="handleSearch">搜索</el-button>
        <el-button type="primary" icon="el-icon-circle-plus-outline" plain @click="handleAdd">添加
        </el-button>
      </div>
      <el-table :data="tableData" border class="table" ref="multipleTable" header-cell-class-name="table-header"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"/>
        <el-table-column prop="id" label="读者ID" sortable fixed></el-table-column>
        <el-table-column prop="loginName" label="用户名"></el-table-column>
        <el-table-column prop="readerName" label="姓名"></el-table-column>
        <el-table-column prop="gender" label="性别"></el-table-column>
        <el-table-column prop="age" label="年龄" sortable></el-table-column>
        <el-table-column prop="phone" label="电话"></el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(scope.$index, scope.row)">编辑
            </el-button>
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

    <!-- 编辑弹出框 -->
    <el-dialog title="编辑"  v-model="editVisible" width="30%">
      <el-form label-width="70px" :model="form" :rules="rules2" ref="refForm2">
        <el-form-item label="用户名" prop="loginName" >
          <el-input v-model="form.loginName"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="readerName">
          <el-input v-model="form.readerName"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender" class="ml-4">
            <el-radio label="男" size="large">男</el-radio>
            <el-radio label="女" size="large">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input type="number" v-model.number="form.age"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editVisible=false">取 消</el-button>
                    <el-button type="primary" @click="saveEdit">确 定</el-button>
                </span>
      </template>
    </el-dialog>
    <!-- 编辑添加弹出框 -->
    <el-dialog title="添加用户" v-model="editVisible2" width="30%" :before-close="closeExpertFormDialog" >
      <el-form label-width="70px" :model="form2" ref="refForm" :rules="rules">
        <el-form-item label="用户名" prop="loginName">
          <el-input v-model="form2.loginName"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form2.password"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="readerName">
          <el-input v-model="form2.readerName"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form2.gender" class="ml-4">
            <el-radio label="男" size="large">男</el-radio>
            <el-radio label="女" size="large">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input type="number" v-model.number="form2.age"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form2.email"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form2.phone"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
                <span class="dialog-footer">
                    <el-button @click="cancel">取 消</el-button>
                    <el-button type="primary" @click="saveAdd">确 定</el-button>
                </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {inject, ref, reactive} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {fetchData} from "../api/index";
import request from "../utils/request";

export default {
  name: "basetable",
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
      request.get(`/admin/readers/${query.pageIndex}/${query.pageSize}/${query.name}`).then(res => {
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
            request.delete(`/admin/readers/delete/${tableData.value[index].id}`).then(res => {
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
    const rules = {
      loginName: [
        {
          required: true,
          message: "请输入用户名",
          trigger: "blur",
        },
        {
          min:6,
          max: 15,
          message: "长度为6-15位字符",
          trigger: "blur",
        },
      ],
      password: [
        { required: true, message: "请输入密码", trigger: "blur" },
      ],
      email: [
        {
          required:true,
          validator:judgemail,
          trigger: "blur"
        }
      ],
    };
    const rules2 = {
      loginName: [
        {
          min:6,
          max: 15,
          message: "长度为6-15位字符",
          trigger: "blur",
        },]
    };
    // 表格编辑时弹窗和保存
    const editVisible = ref(false);
    let form = reactive({
      id: null,
      loginName: "",
      readerName: "",
      gender: "",
      age: null,
      phone: "",
    });
    // 表格编辑时弹窗和保存
    const editVisible2 = ref(false);
    let form2 = reactive({
      id: null,
      loginName: "",
      password:"",
      readerName: "",
      gender: "",
      age: null,
      email:"",
      phone: "",
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
    const handleAdd = () => {
      editVisible2.value = true;
    };
    const refForm2 = ref(null);
    const saveEdit = () => {
      refForm2.value.validate((valid) => {
        if (valid) {
          editVisible.value = false;
          // Object.keys(form).forEach((item) => {
          //   tableData.value[idx][item] = form[item];
          // });
          request.put("/admin/readers/edit", form).then(res => {
            if (res.code === 457) {
              ElMessage.success(`修改第 ${idx + 1} 行成功`);
              getData()
            } else {
              ElMessage.error(res.msg);
            }
          })
        }
      })
    };
    const refForm = ref(null);
    const saveAdd = () => {
      refForm.value.validate((valid) => {
            if (valid) {
              editVisible2.value = false;
              let addId = tableData.value.length;
              // tableData.value.push(form2)
              request.post("/admin/readers/add",form2).then(res => {
                if (res.code === 454) {
                  ElMessage.success(`新增加第${addId + 1} 行成功`);
                  getData()
                  refForm.value.resetFields();
                } else {
                  ElMessage.error(res.msg);
                }
              })
            }else{
              ElMessage.error("增加失败");
              return false;
            }
            })
    };
    const cancel = () => {
      editVisible2.value = false;
      refForm.value.resetFields();
    };
    //用于对话框关闭
    const closeExpertFormDialog = (done) => {
      refForm.value.resetFields();
      done();//done 用于关闭 Dialog
    };
    return {
      rules,
      rules2,
      query,
      tableData,
      pageTotal,
      editVisible,
      editVisible2,
      form,
      form2,
      refForm2,
      refForm,
      renovate,
      handleSearch,
      handlePageChange,
      handleSizeChange,
      handleDelete,
      handleEdit,
      handleAdd,
      saveEdit,
      saveAdd,
      cancel,
      closeExpertFormDialog
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
              request.post("/admin/readers/delete",this.multipleList).then(res => {
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
