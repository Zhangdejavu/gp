package com.zqw.gp.utils;

import com.zqw.gp.component.common.Constants;
import com.zqw.gp.component.entity.DataResult;
import com.zqw.gp.component.entity.LeftTicketDTO;
import com.zqw.gp.component.entity.Ticket;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author zqw
 * @date 2023/3/8 18:28
 */
@Slf4j
public class GpUtils {

    /**
     * 切割12306返回值
     * @param result
     * @return
     */
    public static List<Ticket> splitData(DataResult result){
        List<String> strs = result.getResult();
        Map<String,String> map = result.getMap();
        List<Ticket> list = new ArrayList<>();
        for (String str : strs){
            List<String> data = new ArrayList<>();
            String[] ss = str.split("\\|");
            for (String s : ss){
                if (s.trim().equals("")){
                    continue;
                }
                if (map.containsKey(s)){
                    s = map.get(s);
                }
                data.add(s);
            }
            Ticket ticket = chooseData(data);
            list.add(ticket);
        }
        return list;
    }

    public static Ticket chooseData(List<String> data){
        String dataPrice = null;
        if (data.size()==Constants.TRAIN_F_DATA_LENGTH){
            dataPrice = data.get(27);
        }else {
            dataPrice = data.get(28);
        }
        Map<String,Float> seatPrice = buildSeatPrice(dataPrice);
        Ticket ticket = new Ticket(data.get(2),data.get(3),data.get(6),data.get(7),data.get(4),data.get(5),data.get(8),data.get(9),data.get(10),data.get(23),data.get(22),data.get(21),data.get(20),data.get(13),seatPrice.get("businessSeatPrice"),seatPrice.get("firstSeatPrice"),seatPrice.get("secondSeatPrice"),seatPrice.get("noSeatPrice") );
        log.info("火车编号：{},车次：{}，出发地编号：{}，目的地编号：{}，发车地编号：{},终点编号：{}，出发时间：{}，到站时间：{}，耗时：{}，商务座：{}，一等座：{}，二等座：{}，到站日期，{},商务座价格：{}，一等座价格：{}，二等座价格：{}，无座价格：{}",
                ticket.getTrainCode(),ticket.getTrainNo(),ticket.getFromStation(),ticket.getToStation(),ticket.getStartStation(),ticket.getEndStation(),ticket.getFromTime(),ticket.getToTime(),ticket.getCostTime(),ticket.getBusinessSeat(),ticket.getFirstSeat(),ticket.getSecondSeat(),ticket.getNoSeat(),ticket.getArriveDate(),"商务座价格","一等座价格","二等座价格","无座价格");
        return ticket;
    }

    /**
     * 构建请求头
     * @return
     */
    public static Map<String,String> buildHeaders(LeftTicketDTO dto){
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie","_jc_save_fromStation="+dto.getFrom_station()+";_jc_save_toStation="+dto.getTo_station());
        headers.put("User-Agent",Constants.WEB_USER_AGENT);
        return headers;
    }

    /**
     *  构建请求参数
     * @return
     */
    public static Map<String,String> buildParams(LeftTicketDTO dto) throws IllegalAccessException {
        Map<String,String> paramMap = new LinkedHashMap<>();
//        Class c = dto.getClass();
//        Field[] fields = c.getDeclaredFields();
//        for (int i = 0; i < fields.length; i++){
//            Field field = fields[i];
//            field.setAccessible(true);
//            String[] strings = field.getDeclaringClass().getName().split("\\.");
//            String s = strings[strings.length-1];
//            String name = s.substring(0,1).toLowerCase()+s.substring(1)+"."+field.getName();
//            paramMap.put(name,field.get(dto)+"");
//        }
        paramMap.put("leftTicketDTO.train_date", dto.getTrain_date());
        paramMap.put("leftTicketDTO.from_station",dto.getFrom_station());
        paramMap.put("leftTicketDTO.to_station",dto.getTo_station());
        paramMap.put("purpose_codes",dto.getPurpose_codes());
        return paramMap;
    }

    public static Map<String,Float> buildSeatPrice(String str){
        if (str==null || str.equals("") || str.length()!=Constants.SEAT_PRICE_STR_LENGTH){
            return null;
        }
        Map<String,Float> seatPrice = new HashMap<>();
        seatPrice.put("businessSeatPrice",buildPrice(str.substring(1,6)));
        seatPrice.put("firstSeatPrice",buildPrice(str.substring(12,17)));
        seatPrice.put("secondSeatPrice",buildPrice(str.substring(22,27)));
        seatPrice.put("noSeatPrice",buildPrice(str.substring(32,37)));
        return seatPrice;
    }

    public static float buildPrice(String str){
        float thousand = Integer.valueOf(str.substring(0,1))*1000f;
        float hundred = Integer.valueOf(str.substring(1,2))*100f;
        float ten = Integer.valueOf(str.substring(2,3))*10f;
        float one = Integer.valueOf(str.substring(3,4))*1f;
        float dime = Integer.valueOf(str.substring(4,5))*0.1f;
        return (thousand+hundred+ten+one+dime);
    }
}
