/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public class MultiListInput extends Input {
    public final List<String> list;
    
    public MultiListInput() {
        super(Type.MULTI_LIST);
        list = new ArrayList<>();
    }
    
    public MultiListInput(String... values) {
        super(Type.MULTI_LIST);
        list = Collections.unmodifiableList(Arrays.asList(values));
    }
}
