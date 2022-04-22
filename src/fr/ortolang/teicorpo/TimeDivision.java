/**
 * @author cp 
 * conversion and extraction of time in seconds, etc.
 */

package fr.ortolang.teicorpo;

class TimeDivision {
    public static int toHours(float t) {
        t = t / 60;
        t = t / 60;
        return (int)t;
    }
    public static int toMinutes(float t) {
        t = t / 60;
        return (int)(t % 60);
    }
    public static int toXMinutes(float t) {
        t = t / 60;
        return (int)(t);
    }
    public static int toSeconds(float t) {
        return (int)(t % 60);
    }
    public static int toCentiSeconds(float t) {
        t = ((t*100) % 100);
        return (int)(t);
    }
    public static int toMilliSeconds(float t) {
        return (int)((t*1000.0) % 1000);
    }
    public static int toHours(String s) {
        return toHours(Float.parseFloat(s));
    }
    public static int toMinutes(String s) {
        return toMinutes(Float.parseFloat(s));
    }
    public static int toXMinutes(String s) {
        return toXMinutes(Float.parseFloat(s));
    }
    public static int toSeconds(String s) {
        return toSeconds(Float.parseFloat(s));
    }
    public static int toCentiSeconds(String s) {
        return toCentiSeconds(Float.parseFloat(s));
    }
    public static int toMilliSeconds(String s) {
        return toMilliSeconds(Float.parseFloat(s));
    }
}
