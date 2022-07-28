package com.lib.service.Impl;

import com.lib.common.Result;
import com.lib.common.ResultEnum;
import com.lib.controller.vo.AnalyzeCopyVO;
import com.lib.controller.vo.CateRatio;
import com.lib.controller.vo.WeekDayVO;
import com.lib.entity.bookCategory;
import com.lib.mapper.bookCategoryMapper;
import com.lib.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class StatisticServiceImpl implements StatisticsService {

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public bookCategoryMapper bookCategoryMapper;


    @Override
    public Result getCount() {

        long begin = System.currentTimeMillis();
        Map<String,Object> map = new LinkedHashMap<>();

        //读者总量
        Integer readerCount = redisTemplate.opsForSet().size("lib:ReaderLoginName").intValue();
        map.put("ReaderNum", readerCount);

        //未读消息总量
        Integer waitingApprovesCount = redisTemplate.opsForSet().size("lib:WaitingMessagesList").intValue();
        map.put("WaitingApprovesNum", waitingApprovesCount);

        //书籍总量
        Set<ZSetOperations.TypedTuple> set = redisTemplate.opsForZSet().rangeWithScores("lib:TypeBookCount", 0, -1);
        int cnt = 0;
        for(ZSetOperations.TypedTuple t : set){
            cnt += t.getScore();
        }
        map.put("BookNum", cnt);

        //不同类型比例
        List<bookCategory> bookCategories = bookCategoryMapper.selectAll();
        Map<String, String> function = new HashMap<>();
        for(bookCategory bookCategory : bookCategories){
            function.put("C" + bookCategory.getId(), bookCategory.getCategoryName());
        }

        List<CateRatio> cateRatios = new ArrayList<>();
        for(ZSetOperations.TypedTuple t : set){

            String category = t.getValue().toString();
            String name = function.get(category);
            Double ratio1 = t.getScore()/cnt*100;
            BigDecimal bigDecimal = new BigDecimal(ratio1);
            Double ratio = bigDecimal.setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue();
            CateRatio cateRatio = new CateRatio(name, ratio);
            cateRatios.add(cateRatio);
            System.out.println("种类：" + name + "，数量：" + t.getScore());
            System.out.println("比例为：" + ratio1);
        }

        map.put("TypeBookRatio", cateRatios);

        //分析新增用户
        List<WeekDayVO> weekDayVOS = new ArrayList<>();
        String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        //获取上周周一时间
        Calendar cal = Calendar.getInstance();

        int n = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (n == 0) {
            n = 7;
        }
        cal.add(Calendar.DATE, -(7 + (n - 1)));// 上周一的日期
        Date monday = cal.getTime();
        String realTime = new SimpleDateFormat("yyyy-MM-dd").format(monday);
        Date date = new Date();
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(realTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return Result.create(ResultEnum.QUERY_ERROR, null);
        }

        Long min = date.getTime();
        for(int i = 1; i <= 7; i++){

            cal.setTimeInMillis(min);
            String day = weekDays[i%7];
            cal.add(Calendar.HOUR, 24);

            Long max = cal.getTime().getTime();
            Long count = redisTemplate.opsForZSet().count("lib:stNewReader", min, max);
            WeekDayVO weekDayVO = new WeekDayVO(day,count.intValue());
            weekDayVOS.add(weekDayVO);
            min = max;
        }
        map.put("analyzeNewReader", weekDayVOS);


        //分析副本比例
        List<AnalyzeCopyVO> list = new ArrayList<>();
        Map<String, Integer> entries = redisTemplate.opsForHash().entries("lib:TypeBookCopyCount");
        Set<Map.Entry<String, Integer>> entries1 = entries.entrySet();
        Integer sum = 0;
        for(Map.Entry<String, Integer> entry : entries1){
            sum += entry.getValue();
        }

        for(Map.Entry<String, Integer> entry : entries1){
            AnalyzeCopyVO analyzeCopyVO = new AnalyzeCopyVO();
            analyzeCopyVO.setName(function.get(entry.getKey()));
            analyzeCopyVO.setCount(entry.getValue());

            Double cnt1 = new Double(entry.getValue()*100);
            cnt1 = cnt1 / sum;
            BigDecimal bigDecimal =  new BigDecimal(cnt1);

            analyzeCopyVO.setRatio(bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            list.add(analyzeCopyVO);
        }

        map.put("TypeBookCopyCount", list);

        long end1 = System.currentTimeMillis();
        System.out.println("热门书籍花费:" + (end1 - begin) + "毫秒");


        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        int month = calendar.get(Calendar.MONTH);
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        int lastWeek;
        //分析最近一周书籍借阅情况
        if(week == 1){
            Calendar calendar1 = Calendar.getInstance();
            calendar1.set(Calendar.YEAR, year - 1);
            calendar1.set(Calendar.MONTH, 12);
            calendar1.set(Calendar.DAY_OF_MONTH, 31);
            lastWeek = calendar1.get(Calendar.WEEK_OF_YEAR);
        }else{
            lastWeek = week - 1;
        }
        Map<String, Object> map1 = new HashMap<>();
        map1.put("weekday", weekDays);
        System.out.println("lib:" + year + "." + lastWeek);
        String name1 = "lib:" + year + "." + lastWeek;
        Map<String,Integer> entries2 = redisTemplate.opsForHash().entries(name1);
        Integer[] cnt1 = new Integer[7];
        for(int i = 0; i < 7; i++){
            if(entries2.get(""+i) == null){
                cnt1[i] = 0;
                continue;
            }
            cnt1[i] = (Integer) entries2.get(""+i);
        }
        map1.put("count",cnt1);
        map.put("lastWeek", map1);


        Map<String, Object> map2 = new LinkedHashMap<>();

        //分析最近五个月
        String[] CHMonth = {"一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月"};
        String[] months = new String[5];
        int tempMonth = (month + 7)%12;
        List<List<Integer>> ultList = new ArrayList<>();
        for(int i = 0; i <function.size(); i++){
            ultList.add(new ArrayList<Integer>(5));
        }
        for(int t = 0; t < 5; t++){
            months[t] = CHMonth[tempMonth];
            int realMonth = tempMonth+1;
            Map<String, Integer> entries3 = redisTemplate.opsForHash().entries("lib:" + year + "&" + realMonth);
            for(int j = 0; j < function.size(); j++){
                int p = j +1;
                if(entries3.get("" + p) == null){
                    ultList.get(j).add(0);
                    continue;
                }
                ultList.get(j).add(entries3.get("" + p));
            }
            tempMonth = (tempMonth+1)%12;
        }
        List<Map<String, Object>> list1 = new ArrayList<>();
        System.out.println(ultList.size());
        int c = 1;
        for(List<Integer> l : ultList){
            Map<String, Object> tmap = new LinkedHashMap<>();
            tmap.put(function.get("C" + c), l);
            list1.add(tmap);
            c++;
        }
        map2.put("date",list1);
        map2.put("Months", months);
        map.put("lastFiveMonths", map2);
        return Result.create(ResultEnum.QUERY_SUCCESS, map);
    }

    @Override
    public Result getReaderCount() {
        Integer size = redisTemplate.opsForSet().size("lib:ReaderLoginName").intValue();
        return Result.create(ResultEnum.QUERY_SUCCESS, size);
    }

    @Override
    public Result getWaitingApprovesCount() {
        Integer size = redisTemplate.opsForSet().size("lib:WaitingApprovesList").intValue() - 1;
        return Result.create(ResultEnum.QUERY_SUCCESS, size);
    }

    @Override
    public Result getBookCount() {
        Set<ZSetOperations.TypedTuple> set = redisTemplate.opsForZSet().rangeWithScores("lib:TypeBookCount", 0, -1);
        int cnt = 0;
        for(ZSetOperations.TypedTuple t : set){
            cnt += t.getScore();
        }
        return Result.create(ResultEnum.QUERY_SUCCESS, cnt);
    }

    @Override
    public Result getTypeBookRatio() {
        Set<ZSetOperations.TypedTuple> set = redisTemplate.opsForZSet().rangeWithScores("lib:TypeBookCount", 0, -1);
        int cnt = 0;
        for(ZSetOperations.TypedTuple t : set){
            cnt += t.getScore().intValue();
        }

        List<bookCategory> bookCategories = bookCategoryMapper.selectAll();
        Map<String, String> function = new HashMap<>();
        for(bookCategory bookCategory : bookCategories){
            function.put("C" + bookCategory.getId(), bookCategory.getCategoryName());
        }


        Map<String, Double> map = new HashMap<>();
        for(ZSetOperations.TypedTuple t : set){

            String category = t.getValue().toString();
            String name = function.get(category);
            Double ratio1 = t.getScore()/cnt*100;
            BigDecimal bigDecimal = new BigDecimal(ratio1);
            Double ratio = bigDecimal.setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue();
            map.put(name, ratio);
            System.out.println("种类：" + name + "，数量：" + t.getScore());
            System.out.println("比例为：" + ratio1);
        }

        return Result.create(ResultEnum.QUERY_SUCCESS, map);
    }

    @Override
    public Result analyzeNewReader() {
        Map<String, Long> map = new LinkedHashMap<>();
        String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        //获取上周周一时间
        Calendar cal = Calendar.getInstance();

        int n = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (n == 0) {
            n = 7;
        }
        cal.add(Calendar.DATE, -(7 + (n - 1)));// 上周一的日期
        Date monday = cal.getTime();
        String realTime = new SimpleDateFormat("yyyy-MM-dd").format(monday);
        Date date = new Date();
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(realTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return Result.create(ResultEnum.QUERY_ERROR, null);
        }

        Long min = date.getTime();
        for(int i = 1; i <= 7; i++){

            cal.setTimeInMillis(min);
            String day = weekDays[i%7];
            cal.add(Calendar.HOUR, 24);

            Long max = cal.getTime().getTime();
            Long count = redisTemplate.opsForZSet().count("lib:stNewReader", min, max);
            map.put(day, count);

            min = max;
        }


        return Result.create(ResultEnum.QUERY_SUCCESS, map);
    }

    @Override
    public Result analyzeTypeBookCopyCount() {
        List<bookCategory> bookCategories = bookCategoryMapper.selectAll();
        Map<String ,String> map = new HashMap<>();
        List<AnalyzeCopyVO> list = new ArrayList<>();
        for(bookCategory bookCategory : bookCategories){
            map.put("C" + bookCategory.getId(), bookCategory.getCategoryName());
        }
        Map<String, Integer> entries = redisTemplate.opsForHash().entries("lib:TypeBookCopyCount");
        Set<Map.Entry<String, Integer>> entries1 = entries.entrySet();
        Integer sum = 0;
        for(Map.Entry<String, Integer> entry : entries1){
            sum += entry.getValue();
        }

        for(Map.Entry<String, Integer> entry : entries1){
            AnalyzeCopyVO analyzeCopyVO = new AnalyzeCopyVO();
            analyzeCopyVO.setName(map.get(entry.getKey()));
            analyzeCopyVO.setCount(entry.getValue());

            Double cnt = new Double(entry.getValue()*100);
            cnt = cnt/ sum;
            BigDecimal bigDecimal =  new BigDecimal(cnt);

            analyzeCopyVO.setRatio(bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            list.add(analyzeCopyVO);
        }
        return Result.create(ResultEnum.QUERY_SUCCESS, list);

    }
}

