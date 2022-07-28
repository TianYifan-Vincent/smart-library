  <template>
  <el-row>
    <el-col :span="4" :offset="3">
      <el-card :body-style="{ padding: '0px' }">
        <img :src="book.coverImg" class="image">
        <div style="padding: 14px;text-align:left">
          <span>{{book.title}}</span>
          <div class="bottom clearfix">
            <span class="publish">{{book.publisher}}</span>
          </div>
        </div>
      </el-card>
    </el-col>
    <el-col :span="10" :offset="3">
      <ul class="info">
        <li name="bookName"><label>书名</label><span>{{book.title}}</span></li>
        <li name="author"><label>作者</label><span>{{book.author}}</span></li>
        <li name="publisher"><label>出版社</label><span>{{book.publisher}}</span></li>
        <li name="unitPrice"><label>价格</label><span>{{book.price}}</span></li>
        <li name="sumary"><label>简介</label><span>{{book.description}}</span></li>
        <li name="lastNum"><label>余量</label><span>{{book.bookCopyNumber}}</span></li>
        <li>
        </li>
      </ul>
      <el-button type="primary" @click="borrowBook" :disabled="book.bookCopyNumber<1" >借阅</el-button>
      <!-- 编辑弹出框 -->
      <el-dialog title="编辑"  v-model="editVisible" width="30%" :before-close="closeExpertFormDialog" >
        <el-form label-width="70px" :model="form" ref="refForm">
          <el-form-item label="借阅天数" prop="time" >
            <el-radio-group v-model="form.time">
              <el-radio :label="1">1个月</el-radio>
              <el-radio :label="2">2个月</el-radio>
              <el-radio :label="3">3个月</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
                <span class="dialog-footer">
                    <el-button @click="cancel">取 消</el-button>
                    <el-button type="primary" @click="saveEdit">确 定</el-button>
                </span>
        </template>
      </el-dialog>
    </el-col>
  </el-row>
</template>

<script>
// import { setTimeout } from 'timers';
import request from "../../utils/request";
import {reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";

export default {
  name: "BookInfo",
  setup(){
    const userid=parseInt(localStorage.getItem("ms_userid"))
    const book = ref({});
    const bookId=parseInt(localStorage.getItem("bookId"))
    const getData = () => {
      request.get(`/lib/bookInfo/${bookId}`).then(res => {
        if (res.code === 455) {
            book.value=res.data
        }
      })
    };
    getData();
    // 表格编辑时弹窗和保存
    const editVisible = ref(false);
    const form = reactive({
        bookId:bookId,
        readerId:userid,
        time:null
    });
    const borrowBook=()=>{
      // 二次确认删除
      ElMessageBox.confirm("确定要借阅吗？", "提示", {
        type: "info",
      })
          .then(() => {
            editVisible.value=true
          })
          .catch(() => {
          })
    };
    const refForm = ref(null);
    const cancel = () => {
      editVisible.value = false;
      refForm.value.resetFields();
    };
    //用于对话框关闭
    const closeExpertFormDialog = (done) => {
      refForm.value.resetFields();
      done();//done 用于关闭 Dialog
    };
    const saveEdit = () => {
        editVisible.value = false;
      request.put("/lib/borrow",form).then(res => {
        if (res.code===457 ) {
          ElMessage.success("提交成功");
          refForm.value.resetFields();
        }else{
          ElMessage.error(res.msg)
        }
      })
    };
    return{
      book,
      form,
      editVisible,
      refForm,
      borrowBook,
      cancel,
      closeExpertFormDialog,
      saveEdit
    };
  },
  data() {
    return {

    };
  },
  methods: {

  },
  created: function() {
  }
};
</script>

<style scoped>
.el-row{
  margin-top: 100px;
}
.publish {
  font-size: 13px;
  color: #999;
}

.bottom {
  margin-top: 13px;
  line-height: 12px;
}

.button {
  padding: 0;
  float: right;
}

.image {
  width: 100%;
  display: block;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both;
}
ul {
  list-style: none;
  text-align: center;

}

ul.info li{
  margin: 10px;
  min-height: 30px;
  line-height: 30px;
}
li label{
  float: left;
  font-weight: bold;
}
li span{

}
li[name="sumary"] span{
  text-align: left;
}
li[name="bookName"]{
  font-size: 32px;
  font-weight: bold;
}
</style>

