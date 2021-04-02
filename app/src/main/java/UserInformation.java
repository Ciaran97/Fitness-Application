import java.util.Date;

public class UserInformation {

    public String FName;
    public String Surname;
    public int Weight;
    public int Height;
    public int StepGoal;
    public int WeightGoal;
    public Date WeightGoalDate;
    public int cell;
    public int Age;
    public String Gender;


    public UserInformation(String FName, String surname, int weight, int height, int stepGoal, int weightGoal, Date weightGoalDate, int cell, int age, String gender) {
        this.FName = FName;
        Surname = surname;
        Weight = weight;
        Height = height;
        StepGoal = stepGoal;
        WeightGoal = weightGoal;
        WeightGoalDate = weightGoalDate;
        this.cell = cell;
        Age = age;
        Gender = gender;

    }
}
