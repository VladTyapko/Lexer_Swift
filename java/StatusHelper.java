public class StatusHelper {

    private Status currenStatus;
    private Status previousStatus;

    public StatusHelper() {
        this.currenStatus = Status.START;
        this.previousStatus = currenStatus;
    }

    public void setCurrenStatus(Status newState) {
        previousStatus = currenStatus;
        currenStatus = newState;
    }

    public Status getCurrenStatus() {
        return currenStatus;
    }

    public Status getPreviousStatus() {
        return previousStatus;
    }
}
