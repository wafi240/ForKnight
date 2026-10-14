package com.forknight.client.component;

import com.forknight.common.dto.graph.CommitNode3D;
import com.forknight.common.dto.graph.Edge3D;
import com.forknight.common.dto.graph.GitGraphDataDTO;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;
import com.forknight.client.util.TooltipHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Interactive 3D Commit DAG visualization with JavaFX 3D SubScene, Orbit Controls, and Animations.
 */
public class GitGraph3DView extends StackPane {

    private final Group root3D = new Group();
    private final Group graphGroup = new Group();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    private final Rotate rotateX = new Rotate(-35, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-20, Rotate.Y_AXIS);
    private final Translate cameraTranslate = new Translate(0, 0, 0);

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double mouseAnchorAngleX;
    private double mouseAnchorAngleY;
    private double mouseAnchorTransX;
    private double mouseAnchorTransY;

    private boolean autoRotateEnabled = true;
    private AnimationTimer autoRotateTimer;

    private Consumer<CommitNode3D> onCommitSelected;
    
    // Track node spheres to apply visual effects
    private final Map<String, Sphere> nodeSpheres = new HashMap<>();

    public GitGraph3DView() {
        setStyle("-fx-background-color: transparent;");

        SubScene subScene = new SubScene(root3D, 800, 500, true, SceneAntialiasing.BALANCED);
        subScene.widthProperty().bind(widthProperty());
        subScene.heightProperty().bind(heightProperty());
        
        // Deep space background
        subScene.setFill(Color.web("#05060A"));

        setupCamera(subScene);
        setupLighting();
        setupEnvironment();
        setupMouseControls(subScene);
        setupAutoRotate();

        root3D.getChildren().add(graphGroup);
        getChildren().add(subScene);
    }

    public void setOnCommitSelected(Consumer<CommitNode3D> onCommitSelected) {
        this.onCommitSelected = onCommitSelected;
    }

    private void setupCamera(SubScene subScene) {
        camera.setNearClip(0.1);
        camera.setFarClip(15000.0);
        camera.setTranslateZ(-800.0);
        // camera.setTranslateY(-100.0); removed to center graph

        Group cameraGroup = new Group();
        cameraGroup.getChildren().add(camera);
        cameraGroup.getTransforms().addAll(rotateY, rotateX, cameraTranslate);

        root3D.getChildren().add(cameraGroup);
        subScene.setCamera(camera);
    }

    private void setupLighting() {
        AmbientLight ambientLight = new AmbientLight(Color.rgb(180, 180, 210, 0.6));
        PointLight pointLight1 = new PointLight(Color.WHITE);
        pointLight1.setTranslateX(200);
        pointLight1.setTranslateY(-300);
        pointLight1.setTranslateZ(-400);
        
        PointLight pointLight2 = new PointLight(Color.web("#8b5cf6"));
        pointLight2.setTranslateX(-300);
        pointLight2.setTranslateY(-100);
        pointLight2.setTranslateZ(200);

        root3D.getChildren().addAll(ambientLight, pointLight1, pointLight2);
    }

    private void setupEnvironment() {
        // Add a subtle grid floor
        Group grid = new Group();
        PhongMaterial gridMat = new PhongMaterial(Color.web("#1C1F30", 0.5));
        
        for (int i = -10; i <= 10; i++) {
            Box hLine = new Box(2000, 1, 2);
            hLine.setTranslateZ(i * 100);
            hLine.setMaterial(gridMat);
            
            Box vLine = new Box(2, 1, 2000);
            vLine.setTranslateX(i * 100);
            vLine.setMaterial(gridMat);
            
            grid.getChildren().addAll(hLine, vLine);
        }
        grid.setTranslateY(150);
        root3D.getChildren().add(grid);
    }

    private void setupAutoRotate() {
        autoRotateTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (autoRotateEnabled) {
                    rotateY.setAngle(rotateY.getAngle() + 0.05);
                }
            }
        };
        autoRotateTimer.start();
    }

    private void setupMouseControls(SubScene subScene) {
        subScene.setOnMousePressed(event -> {
            autoRotateEnabled = false; // Disable auto-rotate when user interacts
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
            mouseAnchorAngleX = rotateX.getAngle();
            mouseAnchorAngleY = rotateY.getAngle();
            mouseAnchorTransX = cameraTranslate.getX();
            mouseAnchorTransY = cameraTranslate.getY();
        });

        subScene.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                rotateY.setAngle(mouseAnchorAngleY + (event.getSceneX() - mouseAnchorX) * 0.4);
                rotateX.setAngle(mouseAnchorAngleX - (event.getSceneY() - mouseAnchorY) * 0.4);
            } else if (event.getButton() == MouseButton.SECONDARY) {
                cameraTranslate.setX(mouseAnchorTransX - (event.getSceneX() - mouseAnchorX) * 0.8);
                cameraTranslate.setY(mouseAnchorTransY - (event.getSceneY() - mouseAnchorY) * 0.8);
            }
        });

        subScene.setOnScroll(event -> {
            autoRotateEnabled = false;
            double delta = event.getDeltaY();
            double z = camera.getTranslateZ() + delta * 2.0;
            if (z > -4000 && z < -50) {
                camera.setTranslateZ(z);
            }
        });
    }

    public void renderGraph(GitGraphDataDTO graphData) {
        graphGroup.getChildren().clear();
        nodeSpheres.clear();

        if (graphData == null || graphData.nodes() == null || graphData.nodes().isEmpty()) {
            return;
        }

        Map<String, Point3D> nodePositions = new HashMap<>();

        double minX = graphData.nodes().stream().mapToDouble(CommitNode3D::x).min().orElse(0);
        double maxX = graphData.nodes().stream().mapToDouble(CommitNode3D::x).max().orElse(0);
        double centerX = (minX + maxX) / 2.0;

        for (CommitNode3D node : graphData.nodes()) {
            double nx = node.x() - centerX;
            double ny = node.y();
            double nz = node.z();
            nodePositions.put(node.hash(), new Point3D(nx, ny, nz));

            Sphere sphere = new Sphere(8.0);
            sphere.setTranslateX(nx);
            sphere.setTranslateY(ny);
            sphere.setTranslateZ(nz);

            Color nodeColor = parseColor(node.colorHex());
            PhongMaterial material = new PhongMaterial(nodeColor);
            material.setSpecularColor(Color.WHITE);
            material.setSpecularPower(64.0);
            sphere.setMaterial(material);

            sphere.setCursor(javafx.scene.Cursor.HAND);
            TooltipHelper.install(sphere, "Commit: " + (node.shortHash() != null ? node.shortHash() : node.hash()) + "\n" + node.message());
            
            sphere.setOnMouseClicked(e -> {
                highlightNode(node.hash());
                if (onCommitSelected != null) {
                    onCommitSelected.accept(node);
                }
            });

            nodeSpheres.put(node.hash(), sphere);
            graphGroup.getChildren().add(sphere);
        }

        for (Edge3D edge : graphData.edges()) {
            Point3D p1 = nodePositions.get(edge.fromHash());
            Point3D p2 = nodePositions.get(edge.toHash());

            if (p1 != null && p2 != null) {
                Node connector = createCylinderConnection(p1, p2, parseColor(edge.colorHex()));
                graphGroup.getChildren().add(connector);
            }
        }
    }
    
    public void focusOnCommit(String hash) {
        Sphere sphere = nodeSpheres.get(hash);
        if (sphere != null) {
            highlightNode(hash);
            
            // Smoothly move camera translation to center on this sphere
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(800), 
                    new KeyValue(cameraTranslate.xProperty(), sphere.getTranslateX()),
                    new KeyValue(cameraTranslate.yProperty(), sphere.getTranslateY()),
                    new KeyValue(cameraTranslate.zProperty(), sphere.getTranslateZ())
                )
            );
            timeline.play();
        }
    }
    
    private void highlightNode(String selectedHash) {
        nodeSpheres.forEach((hash, sphere) -> {
            PhongMaterial mat = (PhongMaterial) sphere.getMaterial();
            if (hash.equals(selectedHash)) {
                sphere.setScaleX(1.5);
                sphere.setScaleY(1.5);
                sphere.setScaleZ(1.5);
                mat.setSelfIlluminationMap(null); // Just visual cue
                mat.setSpecularPower(100);
            } else {
                sphere.setScaleX(1.0);
                sphere.setScaleY(1.0);
                sphere.setScaleZ(1.0);
                mat.setSpecularPower(64.0);
            }
        });
    }

    private Node createCylinderConnection(Point3D p1, Point3D p2, Color color) {
        Point3D diff = p2.subtract(p1);
        double length = diff.magnitude();
        if (length < 1e-3) {
            return new Group();
        }

        Point3D mid = p2.add(p1).multiply(0.5);
        Cylinder cylinder = new Cylinder(1.5, length);
        PhongMaterial material = new PhongMaterial(color.deriveColor(0, 1, 1, 0.7));
        material.setSpecularColor(Color.WHITE);
        cylinder.setMaterial(material);

        cylinder.setTranslateX(mid.getX());
        cylinder.setTranslateY(mid.getY());
        cylinder.setTranslateZ(mid.getZ());

        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D axis = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));

        if (axis.magnitude() > 1e-4) {
            cylinder.getTransforms().add(new Rotate(-Math.toDegrees(angle), axis));
        }

        return cylinder;
    }

    private Color parseColor(String hex) {
        try {
            return Color.web(hex);
        } catch (Exception e) {
            return Color.LIGHTSKYBLUE;
        }
    }
}


