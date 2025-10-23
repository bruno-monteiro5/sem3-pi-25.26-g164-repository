package domain;

public class Option {
    private String optionDesc;
    private Runnable optionClass;

    public Option(String optionDesc, Runnable optionClass) {
        this.optionDesc = optionDesc;
        this.optionClass = optionClass;
    }

    @Override
    public String toString() {
        return optionDesc;
    }

    public void executeOption(){
        optionClass.run();
    }
}
