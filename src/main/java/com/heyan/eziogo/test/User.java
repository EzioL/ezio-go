package com.heyan.eziogo.test;

import com.heyan.eziogo.annotations.HeyanBindClass;
import com.heyan.eziogo.annotations.HeyanBindView;

/**
 * Here be dragons Created by Ezio on 2018/2/2 下午5:08
 */
@HeyanBindClass
public class User {
    @HeyanBindView(100)
    int id;
}
