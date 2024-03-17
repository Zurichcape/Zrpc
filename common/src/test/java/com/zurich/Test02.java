package com.zurich;

import com.google.gson.Gson;
import com.zurich.pojo.Address;
import com.zurich.pojo.User;
import org.junit.Test;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 0:27
 * @description
 */

public class Test02 {
    @Test
    public void gsonCopy(){
        Address address = new Address("北京","中国");
        User user = new User("zurich",address);
        Gson gson = new Gson();
        User copyUser = gson.fromJson(gson.toJson(user),User.class);
        user.getAddress().setCity("深圳");
        System.out.println(user == copyUser);
    }


}
