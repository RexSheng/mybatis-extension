package com.github.rexsheng.mybatis.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SQLFormatterUtil {

	public static void main(String[] args){
        String sql="update prod_course set container_no = ?, is_preview = ?, `zone` = ?, `size` = ?, container_category = ?, container_type = ?, company = ?, yard = ?, container_age = ?, container_tare = ?, container_load = ?, container_volume = ?, gross_weight = ?, unit_type = ?, right_angle = ?, is_label = ?, container_owner = ?, container_belong = ?, container_user = ?, container_grade = ?, container_condition = ?, bill_of_lading_no = ?, vessel_voyage = ?, domestic_foreign_trade = ?, is_gon_container = ?, is_freeze_dry = ?, is_surrender = ?, surrender_course = ?, labelling = ?, yard_container_type = ?, yard_cont_grade = ?, lead_sealing = ?, bare_weight = ?, is_cleanout = ?, is_locator = ?, motorcade = ?, car_number = ?, course_type = ?, course_time = ?, add_type = ?, testing_user = ?, testing_state = ?, testing_reject = ?, testing_time = ?, testing_end_time = ?, testing_man_hour_cost = ?, testing_material_cost = ?, testing_thirdparty_money = ?, testing_money = ?, audit_type = ?, audit_user = ?, audit_time = ?, testing_remarks = ?, surrender_type = ?, surrender_bill_type = ?, surrender_test_standard = ?, surrender_no = ?, csc_no = ?, dpp = ?, is_owner_charge = ?, allot_yard = ?, allot_course_type = ?, valuation_no = ?, use_reply_state = ?, use_toreply_user = ?, use_toreply_time = ?, use_reply_user = ?, use_reply_time = ?, master_reply_state = ?, master_toreply_user = ?, master_toreply_time = ?, master_reply_user = ?, master_reply_time = ?, thirdparty_charge_state = ?, already_use_cont_user = ?, valuation_currency = ?, valuation_currency_master = ?, use_exchange_rate = ?, master_exchange_rate = ?, valuation_man_hour_cost = ?, valuation_material_cost = ?, valuation_thirdparty_money = ?, valuation_money = ?, master_reply_money_rmb = ?, master_reply_money_usd = ?, use_reply_money_rmb = ?, use_reply_money_usd = ?, receipts_master_money_rmb = ?, receipts_master_money_usd = ?, receipts_use_money_rmb = ?, receipts_use_money_usd = ?, urgency = ?, valuation_time = ?, is_bill = ?, is_sbs = ?, valuation_user = ?, valuation_reject = ?, valuation_remarks = ?, is_cleaning = ?, designate_user = ?, designate_time = ?, referral_state = ?, finish_user = ?, referral_type = ?, referral_time = ?, referral_user = ?, predict_time = ?, man_hour = ?, finish_time = ?, finish_audit_user = ?, finish_audit_time = ?, referral_remarks = ?, quality_type = ?, quality_state = ?, quality_result = ?, quality_user = ?, quality_time = ?, quality_timeout = ?, pk_quality_state = ?, pk_quality_user = ?, pk_quality_time = ?, pk_quality_remarks = ?, is_out = ?, create_time = ?, create_user = ?, update_time = ?, update_user = ?, signature_pic = ?, testing_photo = ?, speed_photo = ?, after_photo = ?, quality_photo = ?, recheck_photo = ? " + 
        		"where prod_course_id = ?";
        String rlt=formatSql(sql);
        System.err.println(rlt);
    }
	//需要换行的字段
    public static Map<String, Boolean> preMap = new HashMap<String, Boolean>();
    public static Map<String, Boolean> postMap = new HashMap<String, Boolean>();
    public static Map<String, Boolean> map = new HashMap<String, Boolean>();
    public static Map<String, Boolean> bracket= new HashMap<String, Boolean>();//括号前关键字
    static{
    	
    	preMap.put(" from ", false);
    	preMap.put(" inner join ", false);
    	preMap.put(" left join ", false);
    	preMap.put(" right join ", false);
    	preMap.put(" join ", false);
    	preMap.put(" group by ", false);
    	preMap.put(" having ", false);
    	preMap.put(" where ", false);
    	preMap.put(" order by ", false);
    	preMap.put(" limit ", true);
    	preMap.put(" union all ", false);
    	preMap.put(" select ", false);
    	preMap.put(" set ", false);
//    	preMap.put("(", "(");
    	
//    	postMap.put(")", ")");
    	postMap.put(" select ", false);
    	postMap.put(" union all ", false);
    	postMap.put(" set ", false);
    }

    public static String formatSql(String sql){
        sql = sql.trim().replace("\r", "").replace("\n", "").replace("  ", " ");
        while(sql.indexOf("  ")>-1) {
        	sql = sql.trim().replace("  ", " ");
        }
        StringBuilder sb=new StringBuilder();
        String[] sqlArr=sql.split("");
        boolean lineForbidden=false;
        int i=0;
        while(i<sqlArr.length) {
        	String curChar=sqlArr[i];
        	boolean updated=false;
        	
        		for(Entry<String,Boolean> entry:preMap.entrySet()) {
            		int max=(i+entry.getKey().length())>sql.length()?sql.length():(i+entry.getKey().length());
            		if(entry.getKey().equalsIgnoreCase(sql.substring(i,max))){
            			if(!lineForbidden) {
            				sb.append("\r\n");
            			}
            			sb.append(entry.getKey().trim());
            			if(entry.getKey().substring(entry.getKey().length()-1).equals(" ")) {
            				sb.append(" ");
            			}
                		lineForbidden=entry.getValue();
                		i=max-1;
                		updated=true;
            		}
            	}
            	
            	
            	for(Entry<String,Boolean> entry:postMap.entrySet()) {
            		int max=(i+entry.getKey().length())>sql.length()?sql.length():(i+entry.getKey().length());
            		if(entry.getKey().equalsIgnoreCase(sql.substring(i,max))){
            			sb.append(entry.getKey());
            			if(!lineForbidden) {
            				sb.append("\r\n");
            			}
            			if(!entry.getKey().substring(entry.getKey().length()-1).equals(" ")) {
            				sb.append(" ");
            			}
                		lineForbidden=entry.getValue();
                		i=max-1;
                		updated=true;
            		}
            	}
        	
        	if(!updated || lineForbidden) {
        		sb.append(curChar);
        	}
        	i++;
        	        	
        }
        return sb.toString();
    }

        
}
