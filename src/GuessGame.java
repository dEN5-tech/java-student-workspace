import java.util.Scanner;
import java.util.Random;

public class GuessGame {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        int targetNumber = random.nextInt(100) + 1; // Число от 1 до 100
        int attempts = 0;
        int guess = 0;

        System.out.println("==============================================");
        System.out.println("🎯 Игра: Угадай число от 1 до 100!");
        System.out.println("==============================================");

        while (guess != targetNumber) {
            System.out.print("Попробуйте угадать (или введите 0 для выхода): ");
            guess = scanner.nextInt();
            attempts++;

            if (guess == 0) {
                System.out.println("🏳️ Вы сдались! Загаданное число было: " + targetNumber);
                break;
            }

            if (guess < targetNumber) {
                System.out.println("⬆️ Слишком мало! Загаданное число больше.");
            } else if (guess > targetNumber) {
                System.out.println("⬇️ Слишком много! Загаданное число меньше.");
            } else {
                System.out.println("\n🏆 УРА! Вы угадали число " + targetNumber + " всего за " + attempts + " попыток!");
            }
        }

        scanner.close();
    }
}
