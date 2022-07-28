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
  name:"PieChart",
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
      TypeBookCopyCount:[],
      category:[],
      bookdata:[],
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
          this.TypeBookCopyCount=res.data.TypeBookCopyCount
          for(let i of this.TypeBookCopyCount) {
            this.category.push(i.name)
            this.bookdata.push({value: i.count, name: i.name})
          }
          this.$nextTick(() => {
            this.initChart()
          })
        }
      })
    },
    initChart() {
      this.chart = echarts.init(this.$refs.main)
      this.option={
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b} : {c} ({d}%)'
        },
        legend: {
          left: 'center',
          bottom: '10',
          data: this.category
        },
        series: [
          {
            name: 'WEEKLY WRITE ARTICLES',
            type: 'pie',
            roseType: 'radius',
            radius: [15, 95],
            center: ['50%', '38%'],
            data: this.bookdata,
            animationEasing: 'cubicInOut',
            animationDuration: 3000
          }
        ]
      }
      this.chart.setOption(this.option);
    }
  }
}
</script>
