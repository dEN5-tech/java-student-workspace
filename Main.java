import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=========================================");
        System.out.println("🚀 Добро пожаловать в Java Workspace!");
        System.out.println("=========================================");

        System.out.print("👤 Введите ваше имя: ");
        String name = scanner.nextLine();

        System.out.print("🎂 Сколько вам лет? ");
        int age = scanner.nextInt();

        System.out.print("⭐ Ваша любимая оценка (число): ");
        double grade = scanner.nextDouble();

        System.out.println("\n-----------------------------------------");
        System.out.println("🎉 Отлично, " + name + "!");
        System.out.println("• Возраст: " + age + " (через 5 лет будет " + (age + 5) + ")");
        System.out.println("• Любимая оценка: " + grade);
        System.out.println("-----------------------------------------");
        System.out.println("💡 Нажмите Ctrl+Shift+B или F5 для повторного запуска!");

        scanner.close();
    }
}
