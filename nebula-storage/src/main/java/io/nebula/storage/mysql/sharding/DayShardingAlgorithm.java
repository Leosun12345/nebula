//package io.nebula.storage.mysql.sharding;
//
//import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
//import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
//
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//public class DayShardingAlgorithm implements PreciseShardingAlgorithm<Date>, RangeShardingAlgorithm<Date> {
//
//    private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
//    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("M");
//    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("d");
//
//    @Override
//    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Date> shardingValue) {
//        Date date = shardingValue.getValue();
//        String tableName = shardingValue.getLogicTableName();
//        String target = buildTableName(tableName, date);
//
//        if (availableTargetNames.contains(target)) {
//            return target;
//        }
//        throw new IllegalArgumentException("Table not found: " + target);
//    }
//
//    @Override
//    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Date> shardingValue) {
//        List<String> result = new ArrayList<>();
//        Date lower = shardingValue.getValueRange().lowerEndpoint();
//        Date upper = shardingValue.getValueRange().upperEndpoint();
//
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(lower);
//        while (cal.getTime().before(upper)) {
//            String table = buildTableName(shardingValue.getLogicTableName(), cal.getTime());
//            if (availableTargetNames.contains(table)) {
//                result.add(table);
//            }
//            cal.add(Calendar.DAY_OF_YEAR, 1);
//        }
//        return result;
//    }
//
//    private String buildTableName(String logicTable, Date date) {
//        return logicTable + "_" +
//            YEAR_FORMAT.format(date) + "_" +
//            MONTH_FORMAT.format(date) + "_" +
//            DAY_FORMAT.format(date);
//    }
//}
