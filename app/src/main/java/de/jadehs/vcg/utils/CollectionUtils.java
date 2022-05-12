package de.jadehs.vcg.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Nützliche Funktionen im zusammenhang mit Collections
 */
public class CollectionUtils {

    public interface  CollectionPredicate<T>{
        boolean test(T t);
    }

    public interface  CollectionMapper<T,S>{
        S map(T value);
    }


    /**
     * findet den ersten Listeintrag bei dem das gegebene Predicate true zurückgibt
     * @param collection die collection die durchsucht werden soll
     * @param condition die Bedingung
     * @param <T> der typ der Einträge in der Collection
     * @return den ersten Eintrag bei dem das predicate true zurückgibt oder null falls bei allen Einträgen falls zurückgegeben wird
     */
    public static <T> T findFirst(Collection<T> collection, CollectionPredicate<T> condition){

        for(T item : collection){
            if(condition.test(item))
                return item;
        }
        return null;
    }

    /**
     * Iteriert über die gegebene Collection und führt für jeden Eintrag den {@link CollectionMapper} aus, der Rückgabewert wird in eine neue Liste eingetragen die von dieser methode zurückgegeben wird
     * @param collection die Collection, die in eine Liste mit anderem Datentypen gemapped wird
     * @param mapper die Logic die die Einträge in ein anderen Datentyp convertiert
     * @param <T> typ der Einträge in der Eingabe collection
     * @param <U> typ der Einträge in der Ausgabe Liste
     * @return eine neue Liste mit den Werten die von dem Mapper zurückgegeben wird
     */
    public static <T,U> List<U> map(Collection<T> collection,CollectionMapper<T,U> mapper){
        List<U> result = new ArrayList<>();
        for (T value: collection) {
            result.add(mapper.map(value));
        }
        return result;
    }

    public static List<Integer> getRange(int from , int to, int step){
        List<Integer> list = new LinkedList<>();
        for (int i = from; i < to; i+=step) {
            list.add(i);
        }
        return list;
    }

    public static List<Integer> getRange(int from, int to){
        return getRange(from,to,1);
    }

    public static List<Integer> getRange(int to){
        return getRange(0,to);
    }
}
