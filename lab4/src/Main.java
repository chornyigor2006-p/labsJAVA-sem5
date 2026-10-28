import java.util.ArrayList;
import java.util.List;

class VehicleFullException extends Exception {
    public VehicleFullException(String message) { super(message); }
}

class PassengerNotFoundException extends Exception {
    public PassengerNotFoundException(String message) { super(message); }
}

abstract class Human {
    private String name;
    public Human(String name) { this.name = name; }
    public String getName() { return name; }
    @Override
    public String toString() { return this.getClass().getSimpleName() + " " + name; }
}

class RegularPassenger extends Human {
    public RegularPassenger(String name) { super(name); }
}
class Firefighter extends Human {
    public Firefighter(String name) { super(name); }
}
class Policeman extends Human {
    public Policeman(String name) { super(name); }
}

abstract class Vehicle<T extends Human> {
    private int maxSeats;
    private List<T> passengers;

    public Vehicle(int maxSeats) {
        this.maxSeats = maxSeats;
        this.passengers = new ArrayList<>();
    }

    public int getMaxSeats() { return maxSeats; }
    public int getOccupiedSeats() { return passengers.size(); }

    public void board(T passenger) throws VehicleFullException {
        if (passengers.size() >= maxSeats) {
            throw new VehicleFullException("Немає вільних місць для " + passenger.getName() + "!");
        }
        passengers.add(passenger);
        System.out.println(passenger.toString() + " сів у " + this.getClass().getSimpleName());
    }

    public void alight(T passenger) throws PassengerNotFoundException {
        if (!passengers.contains(passenger)) {
            throw new PassengerNotFoundException(passenger.getName() + " не сидить тут!");
        }
        passengers.remove(passenger);
        System.out.println(passenger.toString() + " вийшов з " + this.getClass().getSimpleName());
    }
}

abstract class Car<T extends Human> extends Vehicle<T> {
    public Car(int maxSeats) { super(maxSeats); }
}

class Bus extends Vehicle<Human> {
    public Bus(int maxSeats) { super(maxSeats); }
}

class Taxi extends Car<Human> {
    public Taxi(int maxSeats) { super(maxSeats); }
}

class FireTruck extends Car<Firefighter> {
    public FireTruck(int maxSeats) { super(maxSeats); }
}

class PoliceCar extends Car<Policeman> {
    public PoliceCar(int maxSeats) { super(maxSeats); }
}

class Road {
    // список автомобілів що перевозять будь-яких спадкоємців Human
    public List<Car<? extends Human>> carsInRoad = new ArrayList<>();

    public int getCountOfHumans() {
        int totalHumans = 0;
        for (Car<? extends Human> car : carsInRoad) {
            totalHumans += car.getOccupiedSeats();
        }
        return totalHumans;
    }

    public void addCarToRoad(Car<? extends Human> car) {
        carsInRoad.add(car);
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ДЕМОНСТРАЦІЯ РОБОТИ ПРОГРАМИ ТА ТЕСТИ ===\n");

        try {
            //  створення пасажирів
            RegularPassenger regular = new RegularPassenger("Іван");
            Firefighter firefighter = new Firefighter("Олег");
            Policeman policeman = new Policeman("Андрій");

            //  тест таксі 
            System.out.println("1. Перевірка Таксі:");
            Taxi taxi = new Taxi(3);
            taxi.board(regular);
            taxi.board(firefighter);
            taxi.board(policeman);
            System.out.println("Всі сіли успішно! Зайнято місць: " + taxi.getOccupiedSeats());

            // демонстрація висадки пасажира
            taxi.alight(regular);
            System.out.println("Зайнято місць після висадки: " + taxi.getOccupiedSeats() + "\n");

            // тест спецтранспорту
            System.out.println("2. Перевірка Пожежної та Поліцейської машин:");
            FireTruck fireTruck = new FireTruck(2);
            fireTruck.board(firefighter);

            PoliceCar policeCar = new PoliceCar(2);
            policeCar.board(policeman);
            System.out.println();

            // тест підрахунку людей на дорозі
            System.out.println("3. Перевірка Дороги (Road):");
            Road road = new Road();
            road.addCarToRoad(taxi);
            road.addCarToRoad(fireTruck);
            road.addCarToRoad(policeCar);

            System.out.println("Всього людей в авто на дорозі: " + road.getCountOfHumans() + " (Очікується: 4)\n");

            // перевірка обробки винятків 
            System.out.println("4. Перевірка перевищення кількості місць:");
            PoliceCar smallPoliceCar = new PoliceCar(1);
            smallPoliceCar.board(policeman);
    
            // наступний рядок викличе VehicleFullException
            smallPoliceCar.board(new Policeman("Петро"));

        } catch (VehicleFullException | PassengerNotFoundException e) {
            System.out.println("Спіймано очікуваний виняток: " + e.getMessage());
        }
    }
}