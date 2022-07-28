s<template>
  <div class="wraper" style="margin-top: -30px">
    <el-backtop target=".wraper" :visibility-height="300">
      <div
          style="
           {
            height: 100%;
            width: 100%;
            background-color: #f2f5f6;
            box-shadow: 0 0 6px rgba(0, 0, 0, 0.12);
            text-align: center;
            line-height: 40px;
            color: #1989fa;
          }
        "
      >
        UP
      </div>
    </el-backtop>
    <div class="grid-content">
      <el-row :gutter="20" type="flex" justify="center" style="margin: 50px 0;">
        <el-col :span="8"></el-col>
        <el-col :span="8">
          <!-- 搜索栏 -->
          <el-input
              placeholder="请输入书名关键字"
              v-model="query.keyword"
          >
            <template v-slot:prepend>
              <el-select v-model="query.category" placeholder="分类" class="handle-select mr10">
                <el-option
                    v-for="item in options"
                    :key="item"
                    :label="item"
                    :value="item"
                />
              </el-select>
            </template>
            <template v-slot:append>
              <el-button icon="el-icon-search" @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        <!-- 搜索栏 end -->
        </el-col>
        <el-col :span="8"></el-col>
        <div class="pagination" style="text-align: center">
          <el-pagination background layout="total,sizes, prev, pager, next,jumper" :current-page="query.pageIndex"
                         :page-size="query.pageSize" :total="pageTotal" :page-sizes="[10, 15, 20]"
                         @current-change="handlePageChange" @size-change="handleSizeChange"></el-pagination>
        </div>
      </el-row>
      <el-row v-for="(books,index) in showBooks" :key="index" class="el-row">
        <el-col :span="4" v-for="(book,index) in books" :key="book.id" :offset="index > 0 ?  1 : 0">
          <el-card :body-style="{ padding: '0px' }" style="cursor: pointer;" @click="routeToBook(book.id)">
            <img :src="book.url" class="image">
            <div style="padding: 14px;text-align:left;height: 102px;">
              <span class="title">{{ book.title }}</span>
              <div class="bottom clearfix">
                <span class="publish">{{ book.publisher }}</span>
                <el-button type="text" class="button" @click="routeToBook(book.id)">详情</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import {reactive, ref} from "vue";
import request from "../../utils/request";

export default {
  setup(){
    const query = reactive({
      pageIndex: 1,//当前页号
      pageSize: 10,//每页数量
      keyword: null,//搜索
      category:null//分类
    });
    const books = ref([]);
    const pageTotal = ref(0);
    const options = ref([]);
    const options2 = ref([]);
    // 获取表格数据
    const getData = () => {
      if(query.category==="全部")
        query.category=null
      if (query.keyword==='')
        query.keyword=null
      request.get(`/lib/bookSearch/${query.pageIndex}/${query.pageSize}/${query.keyword}/${query.category}`).then(res => {
        if (res.code===455 ) {
          books.value = res.data.pageInfo.list;
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
    return {
      query,
      books,
      options,
      pageTotal,
      handleSearch,
      handlePageChange,
      handleSizeChange,
    };
  },
  data() {
    return {
      category: [
        { value: "", label: "全部" },
        { value: "经典", label: "经典" },
        { value: "文学", label: "文学" },
        { value: "流行", label: "流行" },
        { value: "科幻", label: "科幻" },
        { value: "政治", label: "政治" },
        { value: "哲学", label: "哲学" },
        { value: "经管", label: "经管" },
      ],
      // showBooks: [{isbn:"123",bookName:"三体",publisher:"刘慈欣",imgUrl:"http://img3m4.ddimg.cn/32/35/23579654-1_u_6.jpg"}],
      sortType: "click",
      // searchForm: {
      //   name: null,
      //   authors: null,
      //   publisher: null,
      //   category: null,
      //   pageSize: 99999,
      //   pageIndex: 1,
      // },
      // editForm: {
      //   id: null,
      //   name: "",
      //   authors: "",
      //   publisher: "",
      //   publishDate: "",
      //   click: "",
      // },
    };
  },
  computed: {
    showBooks () {
      const bookArr = []
      this.books.forEach((item, index) => {
        const row = Math.floor(index / 5)// 4代表4条为一行，随意更改
        if (!bookArr[row]) {
          bookArr[row] = []
        }
        bookArr[row].push(item)
      })
      return bookArr
    }
  },
  methods: {
    routeToBook (id) {
      localStorage.setItem("bookId", id);
      this.$router.push({
        //你需要接受路由的参数再跳
        path: '/book/' + id,
      })
    }
  },
  created () {
  }
};
</script>

<style>
.el-select .el-input {
  width: 80px;
}
.wraper {
  height: 100%;
  overflow-x: hidden;
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
  height: 322px;
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
.el-carousel__item h3 {
  color: #475669;
  opacity: 0.75;
  line-height: 200px;
  margin: 0;
  text-align: center;
}

.el-carousel__item:nth-child(2n) {
  background-color: #99a9bf;
}

.el-carousel__item:nth-child(2n + 1) {
  background-color: #d3dce6;
}
.pagination {
  margin: 20px 0;
  text-align: left;
}
.title{
  height: 38px;
}
.el-row {
  margin-bottom: 30px;
  display: flex;
  flex-wrap: wrap
}
</style>
