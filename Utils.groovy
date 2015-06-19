class Utils {

    public static String getKernelVersion() {

        def process = Runtime.getRuntime().exec("uname -r")
        process.waitFor()

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
        StringBuilder sb = new StringBuilder()

        String line = ""
        while ((line = reader.readLine())!= null) {
            sb.append(line + "\n")
        }

        reader.close()
        return sb
    }

}
