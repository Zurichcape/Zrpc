package com.zurich.compress;

import com.zurich.extension.SPI;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 22:13
 * @description
 */
@SPI
public interface Compress {
    byte[] compress(byte[] bytes);
    byte[] decompress(byte[] bytes);
}
