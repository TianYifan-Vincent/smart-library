<template>
  <div :class="className" :style="{height:height,width:width}"/>
</template>

<script>
import * as echarts from 'echarts';
// import 'echarts/theme/macarons.js'
// require('echarts/theme/macarons') // echarts theme
import resize from '../mixins/resize'

const animationDuration = 6000

export default {
  // name:"BarChart",
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
  data() {
    return {
      chart: null
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart()
    })
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$el, 'macarons')

      this.chart.setOption({
        tooltip: {
          show: true,
          trigger: "axis",
          axisPointer: {
            type: "shadow",   //提示框类型
            label: {       //坐标轴指示器的文本标签
              show: true
            }
          }
        },
        title: {
          text: 'Snow蛋糕店-本周各类蛋糕销量',   //主标题
          textAlign: 'left',    //居左
          textStyle: {         //样式
            fontSize: 20
          }
        },
        grid: {
          top: '15%',
          left: '2%',
          right: '2%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: [{
          type: 'category',
          data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
          axisTick: {
            alignWithLabel: true
          }
        }],
        yAxis: [{
          type: 'value',
          axisTick: {
            show: false
          }
        }],
        series: [{
          name: '男性',
          nameTextStyle: {
            color: "rgb(117,217,204)"    //x轴上方单位的颜色
          },
          type: 'bar',
          stack: 'vistors',
          barWidth: '60%',
          data: [79, 21, 200, 334, 390, 330, 220],
          animationDuration
        }, {
          name: '女性',
          type: 'bar',
          stack: 'vistors',
          barWidth: '60%',
          data: [80, 52, 200, 334, 390, 330, 220],
          animationDuration
        },]
      })
    }
  }
}
</script>
