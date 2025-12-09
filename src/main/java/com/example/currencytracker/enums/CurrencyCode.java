package com.example.currencytracker.enums;

public enum CurrencyCode {
    USD, EUR, PLN, GBP, CHF, JPY, AUD, CAD, CZK, DKK,
    HUF, NOK, SEK, RON, BGN, TRY, CNY, HKD, NZD, SGD,
    ZAR, MXN, BRL, INR, KRW, ILS, IDR, THB, PHP, MYR,
    ISK, AED, SAR, KWD, QAR, ARS, CLP, COP, NGN, EGP,
    UAH, MDL, GEL, BAM, RSD;

    public static boolean isValid(String code) {
        try {
            CurrencyCode.valueOf(code.toUpperCase());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}