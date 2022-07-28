<template>
  <div>
    <div>
      <el-carousel :interval="4000" type="card" height="300px">
        <el-carousel-item v-for="item in posters" :key="item.isbn">
          <img :src="item.imgUrl" style="width: 100%;height: 100%" @click="routeToBook(item.id)">
        </el-carousel-item>
      </el-carousel>
    </div>
    <span class="div1">新书上架</span>
    <el-row>
      <el-col :span="4" v-for="(book,index) in books.newBooks" :key="book.id" :offset="index > 0 ?  1 : 0">
        <el-card :body-style="{ padding: '0px' }" style="cursor: pointer" @click="routeToBook(book.id)">
          <img :src="book.url" class="image">
          <div class="intro">
            <div class="title"><span>{{ book.title }}</span></div>
            <div class="bottom clearfix">
              <span class="publish">{{ book.author }}</span>
              <el-button type="text" class="button" @click="routeToBook(book.id)">详情</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <span class="div1">推荐书籍</span>
    <el-row>
      <el-col :span="4" v-for="(book,index) in books.recBooks" :key="book.id" :offset="index > 0 ?  1 : 0">
        <el-card :body-style="{ padding: '0px' }" style="cursor: pointer" @click="routeToBook(book.id)">
          <img :src="book.url" class="image">
          <div class="intro">
            <div class="title"><span>{{ book.title }}</span></div>
            <div class="bottom clearfix">
              <span class="publish">{{ book.author }}</span>
              <el-button type="text" class="button" @click="routeToBook(book.id)">详情</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <span class="div1">热门书籍</span>
    <el-row>
      <el-col :span="4" v-for="(book,index) in books.hotBooks" :key="book.id" :offset="index > 0 ?  1 : 0">
        <el-card :body-style="{ padding: '0px' }" style="cursor: pointer" @click="routeToBook(book.id)">
          <img :src="book.url" class="image">
          <div class="intro">
            <div class="title"><span>{{ book.title }}</span></div>
            <div class="bottom clearfix">
              <span class="publish">{{ book.author }}</span>
              <el-button type="text" class="button" @click="routeToBook(book.id)">详情</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>


<script>
import request from "../../utils/request";
import {reactive} from "vue";

export default {
  name: "bookslist",
  setup() {
    const userid = localStorage.getItem("ms_userid")
    const books = reactive({
      newBooks: null,
      hotBooks: null,
      recBooks: null
    });
    // 获取表格数据
    const getData = () => {
      request.get(`/lib/home/${userid}`).then(res => {
        if (res.code === 455) {
          books.newBooks = res.data.newBooks
          books.hotBooks = res.data.hotBooks
          books.recBooks = res.data.recBooks
        }
      })
    };
    getData();
    return {
      books
    }
  },
  data() {
    return {
      posters: [{id:62,isbn: "B07PRX6D8S", imgUrl: "../../../static/img/poster1.png"}, {
        id:64,
        isbn: "B07N2GRBY9",
        imgUrl: "../../../static/img/poster4.png"
      }, {id:63,isbn: "B07RHRM1VS", imgUrl: "../../../static/img/poster3.png"}],
    }
  },
  computed: {},
  methods: {
    routeToBook(id) {
      localStorage.setItem("bookId", id);
      this.$router.push({
        //你需要接受路由的参数再跳
        path: '/book/' + id,
      })
    }
  },
  created() {

  }
}
</script>

<style scoped>
.div1 {
  font-weight: bold;
  font-size: large;
}

.el-row {
  margin: 50px 0;
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

.intro {
  padding: 14px;
  text-align: left;
  height: 102px;
}
.title{
  height: 38px;
}
</style>
