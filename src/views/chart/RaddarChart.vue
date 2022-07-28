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
      weekday:[],
      count:[],
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
          this.weekday=res.data.lastWeek.weekday
          this.count=res.data.lastWeek.count
          // console.log(Math.max.apply(null,this.count))//求数组最大值
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
          text: '最近一周图书借阅情况',
          textStyle:{
            color:'#666666',
            textVerticalAlign: "auto"
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            crossStyle: {
              color: '#999'
            }
          }
        },
        toolbox: {
          feature: {
            // dataView: { show: true, readOnly: false },
            // magicType: { show: true, type: ['line', 'bar'] },
            // restore: { show: true },
            // saveAsImage: { show: true }
          }
        },
        legend: {
          show:false,
          data: ['Evaporation', 'Precipitation', 'Temperature']
        },
        xAxis: [
          {
            type: 'category',
            data: this.weekday,
            axisPointer: {
              type: 'shadow'
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            // name: '新增数',
            min: 0,
            max: Math.ceil(Math.max.apply(null,this.count)*1.5/100)*100,
            interval: Math.ceil(Math.max.apply(null,this.count)*1.5/100)*100/5,
            // axisLabel: {
            //   formatter: '{value} ml'
            // }
          },
        ],
        series: [
          {
            name: '新增量',
            color:'#fc8452',
            type: 'bar',
            data: this.count
          },
        ]
      };
      this.chart.setOption(this.option);
    }
  }
}
</script>
