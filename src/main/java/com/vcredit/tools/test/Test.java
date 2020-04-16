package com.vcredit.tools.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: tangdandan
 * @Date: 2020/4/9 17:44
 */
public class Test {
    public static void main(String[] args) {
        List<String > list1 = Arrays.asList("1","2","33","4","5","6","6","6");
        ArrayList res = new ArrayList<String>();
       /* List<String > list2 = Arrays.asList("1","2","3","4","5","8");
        List<String> res =new ArrayList<>();
        Test test = new Test();
        List<String> list = test.compare(list1, list2, res);
        System.out.println(list.toString());*/
        Test test = new Test();
        test.findRepeatValues(list1,res);
    }

    private List<String> compare(List<String> l0, List<String> l1,List<String> res) {
        for (String o : l0) {
            if (!l1.contains(o)) {
                res.add(o);
            }
        }
        for (String o : l1) {
            if (!l0.contains(o)) {
                res.add(o);
            }
        }
        return res;
    }
    public void findRepeatValues(List<String> list, ArrayList res) {
        ArrayList repeat = new ArrayList<String>();
        list.stream().forEach(d->{
            if(repeat.contains(d)){
                res.add(d);
            }else {
                repeat.add(d);
            }
        });
    }
}
