<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-reading"></i> 图书
                </el-breadcrumb-item>
                <el-breadcrumb-item>图书管理</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
      <div class="container">
        <div class="handle-box">
          <el-button type="danger" icon="el-icon-delete" plain @click="handleDelete2">批量删除</el-button>
          <el-select v-model="query.category" placeholder="分类" class="handle-select mr10">
            <el-option
                v-for="item in options"
                :key="item"
                :label="item"
                :value="item"
            />
          </el-select>
          <el-input v-model="query.keyword" placeholder="如输入书名" class="handle-input mr10"></el-input>
          <el-button type="primary" icon="el-icon-search"  @click="handleSearch">搜索</el-button>
          <el-button type="primary" icon="el-icon-circle-plus-outline" plain @click="handleAdd">添加
          </el-button>
        </div>
        <el-table :data="tableData" border class="table" ref="multipleTable" header-cell-class-name="table-header"
                  @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center"/>
          <el-table-column prop="id" label="图书ID" width="100" sortable fixed></el-table-column>
          <el-table-column prop="title" label="书名" width="120"></el-table-column>
          <el-table-column prop="isbn" label="ISBN" width="100"></el-table-column>
          <el-table-column prop="coverImg" label="封面" width="120">
<!--            <template #default="scope">-->
<!--              <el-popover placement="top-start" title="" trigger="hover">-->
<!--                <img :src="scope.row.cover_img" alt="" style="width: 150px;height: 150px">-->
<!--                <img slot="reference" :src="scope.row.cover_img" style="width: 30px;height: 30px">-->
<!--              </el-popover>-->
<!--            </template>-->
            <template #default="scope">
            <el-popover
                popper-style="box-shadow: rgb(14 18 22 / 35%) 0px 10px 38px -10px, rgb(14 18 22 / 20%) 0px 10px 20px -15px; padding: 20px;"
                trigger="hover"
            >
              <template #reference>
                <el-avatar fit="fill" shape="square" :src="scope.row.coverImg" />
              </template>
              <template #default>
                <div
                    class="demo-rich-conent"
                    style="display: flex; gap: 16px; flex-direction: column"
                >
                  <el-avatar
                      fit="fill"
                      :size="120"
                      shape="square" :src="scope.row.coverImg"
                      style="margin-bottom: 8px"
                  />
                  <div>
                    <p class="demo-rich-content__desc" style="margin: 0">
                      {{scope.row.description}}
                    </p>
                  </div>
                </div>
              </template>
            </el-popover>
            </template>
          </el-table-column>
<!--          <el-table-column prop="description" label="简介"></el-table-column>-->
          <el-table-column prop="author" label="作者" width="120"></el-table-column>
          <el-table-column prop="price" label="价格" sortable ></el-table-column>
          <el-table-column prop="category" label="分类"></el-table-column>
          <el-table-column prop="bookCopyNumber" label="复本数" width="100" sortable></el-table-column>
          <el-table-column prop="publisher" label="出版社"></el-table-column>
          <el-table-column prop="publishDate" label="出版日期" width="120"></el-table-column>
          <el-table-column prop="createdTime" label="入库时间" sortable width="160" :formatter="dateFormat"></el-table-column>
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
      <el-dialog title="编辑" v-model="editVisible" width="30%">
        <el-form label-width="70px">
          <el-form-item label="书名">
            <el-input v-model="form.title"></el-input>
          </el-form-item>
          <el-form-item label="ISBN">
            <el-input v-model="form.isbn"></el-input>
          </el-form-item>
          <el-form-item label="封面">
            <el-input v-model="form.coverImg"></el-input>
          </el-form-item>
          <el-form-item label="作者">
            <el-input v-model="form.author"></el-input>
          </el-form-item>
          <el-form-item label="价格">
            <el-input v-model="form.price"></el-input>
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="form.category" placeholder="图书类别" class="handle-select mr10">
              <el-option
                  v-for="item in options2"
                  :key="item"
                  :label="item"
                  :value="item"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="复本数">
            <el-input v-model="form.bookCopyNumber" type="number"></el-input>
          </el-form-item>
          <el-form-item label="出版社">
            <el-input v-model="form.publisher"></el-input>
          </el-form-item>
          <el-form-item label="出版日期">
            <el-input v-model="form.publishDate"></el-input>
          </el-form-item>
          <el-form-item label="描述" prop="description">
            <el-input v-model="form.description"></el-input>
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
      <el-dialog title="添加图书" v-model="editVisible2" width="30%" :before-close="closeExpertFormDialog">
        <el-form label-width="70px" :model="form2" ref="refForm">
          <el-form-item label="书名" prop="title">
            <el-input v-model="form2.title"></el-input>
          </el-form-item>
          <el-form-item label="ISBN" prop="isbn">
            <el-input v-model="form2.isbn"></el-input>
          </el-form-item>
          <el-form-item label="封面" prop="coverImg">
            <el-input v-model="form2.coverImg"></el-input>
          </el-form-item>
          <el-form-item label="作者" prop="author">
            <el-input v-model="form2.author"></el-input>
          </el-form-item>
          <el-form-item label="价格" prop="price">
            <el-input v-model="form2.price"></el-input>
          </el-form-item>
          <el-form-item label="分类" prop="category">
            <el-select v-model="form2.category" placeholder="图书类别" class="handle-select mr10">
              <el-option
                  v-for="item in options2"
                  :key="item"
                  :label="item"
                  :value="item"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="复本数" prop="bookCopyNumber">
            <el-input v-model="form2.bookCopyNumber" type="number"></el-input>
          </el-form-item>
          <el-form-item label="出版社" prop="publisher">
            <el-input v-model="form2.publisher"></el-input>
          </el-form-item>
          <el-form-item label="出版日期" prop="publishDate">
            <el-input v-model="form2.publishDate"></el-input>
          </el-form-item>
          <el-form-item label="描述" prop="description">
            <el-input v-model="form2.description"></el-input>
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
  name: "baseform",
  setup() {
    const query = reactive({
      pageIndex: 1,//当前页号
      pageSize: 10,//每页数量
      keyword: null,//搜索
      category:null//分类
    });
    const tableData = ref([]);
    const pageTotal = ref(0);
    const options = ref([]);
    const options2 = ref([]);
    // 获取表格数据
    const getData = () => {
      if(query.category==="全部")
        query.category=null
      if (query.keyword==='')
        query.keyword=null
      request.get(`/admin/books/${query.pageIndex}/${query.pageSize}/${query.keyword}/${query.category}`).then(res => {
        if (res.code===455 ) {
          tableData.value = res.data.pageInfo.list;
          pageTotal.value = res.data.pageInfo.total;
          options2.value=res.data.categoryName
          let newMsg=JSON.parse(JSON.stringify(res.data.categoryName))//单独开辟空间解决unshift错误
          options.value=newMsg
          options.value.unshift("全部")
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
            request.delete(`/admin/books/delete/${tableData.value[index].id}/${tableData.value[index].category}`).then(res => {
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
      isbn: "",
      coverImg: "",
      description: "",
      author: "",
      price:"",
      category:"",
      bookCopyNumber:"",
      publisher:"",
      publishDate:"",
      userId:"",
    });
    // 表格编辑时弹窗和保存
    const editVisible2 = ref(false);
    let form2 = reactive({
      id: null,
      title: "",
      isbn: "",
      coverImg: "",
      description: "",
      author: "",
      price:"",
      category:"",
      bookCopyNumber:"",
      publisher:"",
      publishDate:"",
      userId:"",
    });
    let idx = -1;
    const handleEdit = (index, row) => {
      idx = index;
      Object.keys(form).forEach((item) => {
        form[item] = row[item].toString();
      });
      editVisible.value = true;
    };
    const renovate = inject("reload");
    const handleAdd = () => {
      editVisible2.value = true;
    };
    const userid = localStorage.getItem("ms_userid");//管理员id
    const saveEdit = () => {
      editVisible.value = false;
      form.userId=userid;
      request.put("/admin/books/edit", form).then(res => {
        if (res.code === 457) {
          ElMessage.success(`修改第 ${idx + 1} 行成功`);
          getData()
        } else {
          ElMessage.error(res.msg);
        }
      })
      // Object.keys(form).forEach((item) => {
      //   tableData.value[idx][item] = form[item];
      // });
    };
    const refForm = ref(null);
    const saveAdd = () => {
      editVisible2.value = false;
      let addId = tableData.value.length;
      form2.userId=userid;
      request.post("/admin/books/add",form2).then(res => {
        if (res.code === 454) {
          ElMessage.success("新增加书籍成功");
          getData()
          refForm.value.resetFields();
        } else {
          ElMessage.error(res.msg);
        }
      })
      // tableData.value.push(form2)
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
    const  dateFormat=(row, column, cellValue, index)=>{
      const daterc = row[column.property]
      if (daterc != null) {
        var date = new Date(daterc);
        var year = date.getFullYear();
        /* 在日期格式中，月份是从0开始，11结束，因此要加0
         * 使用三元表达式在小于10的前面加0，以达到格式统一  如 09:11:05
         * */
        var month = date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1;
        var day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
        var hours = date.getHours() < 10 ? "0" + date.getHours() : date.getHours();
        var minutes = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
        var seconds = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
        // 拼接
        return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
      }
    };
    return {
      options,
      options2,
      query,
      tableData,
      pageTotal,
      editVisible,
      editVisible2,
      form,
      form2,
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
      closeExpertFormDialog,
      dateFormat
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
              request.post("/admin/books/delete",this.multipleList).then(res => {
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
  margin-left: 10px;
  width: 120px;
}

.handle-input {
  margin-left: 5px;
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
/deep/ .el-avatar>img {
  width: 100%;
  display: block;
  height: 100%;
  vertical-align: middle;
}
</style>
