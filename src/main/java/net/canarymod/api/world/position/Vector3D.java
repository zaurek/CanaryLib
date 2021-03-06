package net.canarymod.api.world.position;

public class Vector3D {
    protected double x, y, z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(int x, int y, int z) {
        this.x = (double) x;
        this.y = (double) y;
        this.z = (double) z;
    }

    public Vector3D(float x, float y, float z) {
        this.x = (double) x;
        this.y = (double) y;
        this.z = (double) z;
    }

    public Vector3D() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    /**
     * Copy constructor copies the primitives
     * 
     * @param key
     */
    public Vector3D(Vector3D templ) {
        this.x = templ.x;
        this.y = templ.y;
        this.z = templ.z;
    }

    /**
     * Retrieve X component of Vector
     * 
     * @return double x
     */
    public double getX() {
        return x;
    }

    /**
     * Set x component with native double
     * 
     * @param x
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Set x component with a int2double conversion
     * 
     * @param x
     */
    public void setX(int x) {
        this.x = (double) x;
    }

    /**
     * Retrieve Y component of Vector
     * 
     * @return double y
     */
    public double getY() {
        return y;
    }

    /**
     * Set y component with native double
     * 
     * @param y
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Set y component with a int2double conversion
     * 
     * @param y
     */
    public void setY(int y) {
        this.y = (double) y;
    }

    /**
     * Retrieve Z component of Vector
     * 
     * @return double z
     */
    public double getZ() {
        return z;
    }

    /**
     * Set y component with native double
     * 
     * @param z
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Set z component with a int2double conversion
     * 
     * @param z
     */
    public void setZ(int z) {
        this.z = (double) z;
    }

    /**
     * Get the distance between this and the given vector
     * 
     * @param v
     * @return
     */
    public double getDistance(Vector3D v) {
        double distPower = (Math.pow(v.getX() - this.getX(), 2) + Math.pow(v.getY() - this.getY(), 2) + Math.pow(v.getZ() - this.getZ(), 2));
        return Math.sqrt(distPower);
    }

    /**
     * Retrieve the distance between 2 given vectors
     * 
     * @param v
     * @return double The Distance
     */
    public static double getDistance(Vector3D v1, Vector3D v2) {
        double distPower = (Math.pow(v1.getX() - v2.getX(), 2) + Math.pow(v1.getY() - v2.getY(), 2) + Math.pow(v1.getZ() - v2.getZ(), 2));
        return Math.sqrt(distPower);
    }

    /**
     * Checks if another object equals this one
     * 
     * @param obj
     * @return whether the other object has the same values for x,y,z
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector3D)) {
            return false;
        }
        Vector3D other = (Vector3D) obj;
        return other.getX() == this.x && other.getY() == this.y && other.getZ() == this.z;

    }

    /**
     * Return a hashcode for this object
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = (int) (hash + x);
        hash = (int) (hash + y);
        hash = (int) (hash + z);
        return hash;
    }
    
    public String toString() {
        StringBuilder format = new StringBuilder();
        format.append(this.x).append(":")
                .append(this.y).append(":")
                .append(this.z);
        return format.toString();
    }
}
