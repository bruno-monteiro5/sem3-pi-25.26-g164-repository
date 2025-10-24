package domain;

/**
 * Represents an executable option in a menu system.
 * <p>
 * Each {@code Option} has a textual description and an associated action
 * represented by a {@link Runnable} instance. When the option is selected,
 * the associated action can be executed via {@link #executeOption()}.
 * <p>
 * This class is commonly used in console or UI-based menu structures
 * where each option performs a specific task or launches a related feature.
 */
public class Option {

    /** The textual description displayed for this option. */
    private String optionDesc;

    /** The executable action associated with this option. */
    private Runnable optionClass;

    /**
     * Constructs a new {@code Option} with a given description and runnable action.
     *
     * @param optionDesc  the text describing this option
     * @param optionClass the action to be executed when this option is selected
     */
    public Option(String optionDesc, Runnable optionClass) {
        this.optionDesc = optionDesc;
        this.optionClass = optionClass;
    }

    /**
     * Returns a string representation of this option.
     * <p>
     * By default, this method returns only the option description,
     * which is suitable for display in menus.
     *
     * @return the description text of this option
     */
    @Override
    public String toString() {
        return optionDesc;
    }

    /**
     * Executes the action associated with this option.
     * <p>
     * This method calls {@link Runnable#run()} on the stored runnable object.
     * It is expected that {@code optionClass} is never {@code null}.
     */
    public void executeOption() {
        optionClass.run();
    }
}
