<template>
  <div :class="className" :style="{height:height,width:width}" ref="main"/>
</template>

<script>
import * as echarts from 'echarts';
// import 'echarts/theme/macarons.js'
// require('echarts/theme/macarons') // echarts theme
import resize from '../mixins/resize'
import request from "../../utils/request";
import {reactive, ref, toRaw} from "vue";
import {useStore} from "vuex";
export default {
  name:"LineChart",
  mixins: [resize],
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '300px'
    }
  },
  setup() {
    const store = useStore();
    return{
      store
    }
  },
  computed: {
  },
  created() {
    this.getdata()
  },
  data() {
    return {
      lastFiveMonths:[],
      category:[],
      bookdata:[],
      months:[],
      chart: null,
      option:null,
    }
  },
  mounted() {

  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    getdata(){
      request.get("/admin/statistics/Count").then(res => {
        if (res.code === 455) {
          this.lastFiveMonths=res.data.lastFiveMonths.date
          this.months=res.data.lastFiveMonths.Months
          for(let i of this.lastFiveMonths) {
            this.category.push(Object.keys(i)[0])
            this.bookdata.push(Object.values(i)[0])
          }
          // console.log(this.category,this.bookdata)
          this.$nextTick(() => {
            this.initChart()
          })
        }
      })
    },
    initChart() {
      this.chart = echarts.init(this.$refs.main)
      this.option = {
        title: {
          text: '最近五个月各类书籍借阅趋势图'
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          top:'30px',
          type:'scroll',
          data: this.category
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.months
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: this.category[0],
            type: 'line',
            stack: 'Total',
            data: this.bookdata[0]
          },
          {
            name: this.category[1],
            type: 'line',
            stack: 'Total',
            data: this.bookdata[1]
          },          {
            name: this.category[2],
            type: 'line',
            stack: 'Total',
            data: this.bookdata[2]
          },          {
            name: this.category[3],
            type: 'line',
            stack: 'Total',
            data: this.bookdata[3]
          },          {
            name: this.category[4],
            type: 'line',
            stack: 'Total',
            data: this.bookdata[4]
          },          {
            name: this.category[5],
            type: 'line',
            stack: 'Total',
            data: this.bookdata[5]
          },          {
            name: this.category[6],
            type: 'line',
            stack: 'Total',
            data: this.bookdata[6]
          },          {
            name: this.category[7],
            type: 'line',
            stack: 'Total',
            data: this.bookdata[7]
          },          {
            name: this.category[8],
            type: 'line',
            stack: 'Total',
            data: this.bookdata[8]
          },
          {
            name: this.category[9],
            type: 'line',
            stack: 'Total',
            data: this.bookdata[9]
          },
        ]
      };
      this.chart.setOption(this.option);
    }
  }
}
</script>
