package org.example.model.manytomany;




import org.example.model.Access;
import org.example.model.House;
import org.example.model.Person;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    public static void main( String[] args )
    {
        //  This service takes a many-to-many relationship between people and homes and materializes it as two one-to-many views.
        //  The inputs are two master lists (people and homes) plus a list of access links (personId, homeId).
        //  Using the IDs, the service resolves each link to real objects and builds:

        //  Map<Person, Set<Home>> — for each person, the set of homes they can access.
        //  Map<Home, Set<Person>> — for each home, the set of people who can access it.
        
        Person p1 = Person.builder().name("Victor").lastName("RM").id(1L).build();
        Person p2 = Person.builder().name("Pepe").lastName("Smith").id(2L).build();
        Person p3 = Person.builder().name("Juana").lastName("Santos").id(3L).build();

        List<Person> people = List.of(p1, p2, p3);

        House h1 = House.builder().address("Street 1").code("code1").id(1L).build();
        House h2 = House.builder().address("Street 2").code("code2").id(2L).build();
        House h3 = House.builder().address("Street 3").code("code3").id(3L).build();

        List<House> houseList = List.of(h1, h2, h3);

        Access a1 = Access.builder().houseId(1L).personId(1L).build();
        Access a2 = Access.builder().houseId(2L).personId(1L).build();
        Access a3 = Access.builder().houseId(2L).personId(2L).build();
        Access a4 = Access.builder().houseId(3L).personId(3L).build();

        List<Access> accessList = List.of(a1, a2, a3, a4);

        Map<Long, Person> personaById = people.stream()
                .collect(Collectors.toMap(
                        Person::getId,
                        Function.identity(),
                        (a, b) -> a,            // cómo resolver colisiones de clave (no debería ocurrir)
                        LinkedHashMap::new      // preserva orden de inserción
                ));

        Map<Long, House> viviendaById = houseList.stream()
                .collect(Collectors.toMap(
                        House::getId,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        // 2) Person -> Houses (1:N)
        Map<Person, Set<House>> personasConViviendas =
                accessList.stream()
                        .map(a -> {
                            Person p = personaById.get(a.getPersonId());   // <- aquí usas el índice
                            House v = viviendaById.get(a.getHouseId()); // <- y aquí
                            return (p != null && v != null) ? Map.entry(p, v) : null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(
                                Map.Entry::getKey,
                                LinkedHashMap::new,
                                Collectors.mapping(Map.Entry::getValue,
                                        Collectors.toCollection(LinkedHashSet::new)) // Set para deduplicar
                        ));

        // 3) Houses -> Person (1:N)
        Map<House, Set<Person>> viviendasConPersonas =
                accessList.stream()
                        .map(a -> {
                            Person p = personaById.get(a.getPersonId());
                            House v = viviendaById.get(a.getHouseId());
                            return (p != null && v != null) ? Map.entry(v, p) : null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(
                                Map.Entry::getKey,
                                LinkedHashMap::new,
                                Collectors.mapping(Map.Entry::getValue,
                                        Collectors.toCollection(LinkedHashSet::new))
                        ));

        // 4) Asegura que aparezcan claves sin relaciones (sets vacíos)
        people.forEach(p -> personasConViviendas.computeIfAbsent(p, k -> new LinkedHashSet<>()));
        houseList.forEach(v -> viviendasConPersonas.computeIfAbsent(v, k -> new LinkedHashSet<>()));


        System.out.println("People -> Houses:");
        System.out.println("**********************************");
        personasConViviendas.forEach((person, houses) -> {
            System.out.println("--"+person.toString());
            houses.forEach(house -> {
                System.out.println("----House:"+ house.toString());
            });
        });

        System.out.println("Houses -> People:");
        System.out.println("**********************************");
        viviendasConPersonas.forEach((house, people1) -> {
            System.out.println("--"+house);
            people1.forEach(person -> {
                System.out.println("----Person:"+ person.toString());
            });
        });
    }
}
