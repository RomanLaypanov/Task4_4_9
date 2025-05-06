import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("SpyLogger");
        MailService spy = new Spy(logger);
        Thief thief = new Thief(10);
        MailService inspector = new Inspector();

        MailService[] services = {spy, thief, inspector};
        UntrustworthyMailWorker worker = new UntrustworthyMailWorker(services);

        MailMessage mailMessage = new MailMessage("Austin Powers", "Piter", "Pismo");
        MailMessage mailMessage1 = new MailMessage("Roman", "Piter", "Pismo");

        worker.processMail(mailMessage1);


        Package pack = new Package("Vaza", 100);
        MailPackage mailPackage = new MailPackage("Roman", "Piter", pack);

        worker.processMail(mailPackage);

        Thief theif1 = thief;
        System.out.println("Общая сумма похищенных ценностей: " + theif1.getStolenValue());
    }

    public interface Sendable {
        String getFrom();

        String getTo();
    }

    public static abstract class AbstractSendable implements Sendable {

        protected final String from;
        protected final String to;

        public AbstractSendable(String from, String to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public String getFrom() {
            return from;
        }

        @Override
        public String getTo() {
            return to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AbstractSendable that = (AbstractSendable) o;

            if (!from.equals(that.from)) return false;
            if (!to.equals(that.to)) return false;

            return true;
        }
    }

    public static class MailMessage extends AbstractSendable {

        private final String message;

        public MailMessage(String from, String to, String message) {
            super(from, to);
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            MailMessage that = (MailMessage) o;

            if (message != null ? !message.equals(that.message) : that.message != null) return false;

            return true;
        }
    }

    public static class MailPackage extends AbstractSendable {
        private final Package content;

        public MailPackage(String from, String to, Package content) {
            super(from, to);
            this.content = content;
        }

        public Package getContent() {
            return content;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            MailPackage that = (MailPackage) o;

            if (!content.equals(that.content)) return false;

            return true;
        }
    }

    public static class Package {
        private final String content;
        private final int price;

        public Package(String content, int price) {
            this.content = content;
            this.price = price;
        }

        public String getContent() {
            return content;
        }

        public int getPrice() {
            return price;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Package aPackage = (Package) o;

            if (price != aPackage.price) return false;
            if (!content.equals(aPackage.content)) return false;

            return true;
        }
    }

    public interface MailService {
        Sendable processMail(Sendable mail);
    }

    public static class RealMailService implements MailService {

        @Override
        public Sendable processMail(Sendable mail) {
            return mail;
        }
    }

    public static class UntrustworthyMailWorker implements MailService {
        private final MailService[] mailServices;
        private final RealMailService realMailService = new RealMailService();

        public UntrustworthyMailWorker(MailService[] mailServices) {
            this.mailServices = mailServices;
        }

        @Override
        public Sendable processMail(Sendable mail) {
            for (MailService service : mailServices) {
                mail = service.processMail(mail);
            }

            return realMailService.processMail(mail);
        }

        public RealMailService getRealMailService() {
            return realMailService;
        }
    }

    public static class Spy implements MailService {
        private final Logger logger;

        public Spy(Logger logger) {
            this.logger = logger;
        }

        @Override
        public Sendable processMail(Sendable mail) {
            if (!(mail instanceof MailMessage)) {
                return mail;
            }

            MailMessage message = (MailMessage) mail;
            Logger LOG = Logger.getLogger(Main.class.getName());
            if (message.getFrom().equals("Austin Powers") || message.getTo().equals("Austin Powers")) {
                LOG.warning("Detected target mail correspondence: from " + message.getFrom() +
                        " to " + message.getTo() + ": \"" + message.getMessage() + "\"");
            } else {
                LOG.info("Usual correspondence: from " + message.getFrom() + " to " + message.getTo());
            }

            return mail;
        }
    }

    public static class Thief implements MailService {
        private final int minPrice;
        private int stolenValue = 0;

        public Thief(int minPrice) {
            this.minPrice = minPrice;
        }

        @Override
        public Sendable processMail(Sendable mail) {
            if (!(mail instanceof MailPackage)) {
                return mail;
            }

            MailPackage originalPackage = (MailPackage) mail;
            if (originalPackage.getContent().getPrice() >= minPrice) {
                // Изменяем содержание посылки на камни
                Package fakeContent = new Package("stones instead of " + originalPackage.getContent().getContent(), 0);
                MailPackage fakePackage = new MailPackage(originalPackage.getFrom(), originalPackage.getTo(), fakeContent);

                // Запоминаем стоимость украденного
                stolenValue += originalPackage.getContent().getPrice();
                return fakePackage;
            }

            return mail;
        }

        public int getStolenValue() {
            return stolenValue;
        }
    }


    public static class Inspector implements MailService {
        @Override
        public Sendable processMail(Sendable mail) {
            if (!(mail instanceof MailPackage)) {
                return mail;
            }

            try {
                MailPackage mailPackage = (MailPackage) mail;

                if (mailPackage.getContent().getContent().equals("weapons") ||
                        mailPackage.getContent().getContent().equals("banned substance")) {
                    throw new llegalPackageException("Запрещенный предмет");
                } else if (mailPackage.getContent().getContent().contains("stones")) {
                    throw new StolenPackageException("Посылку украли");
                }
            } catch (llegalPackageException | StolenPackageException e) {
                System.err.println("Ошибка при обработке отправления: " + e.getMessage());
            }

            return mail;
        }
    }
}



